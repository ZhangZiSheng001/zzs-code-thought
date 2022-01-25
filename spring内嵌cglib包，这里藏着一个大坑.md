# 问题发现

2022-01-21 早上 9 点，订单系统出现大面积的“系统未知错误”报错，导致部分用户无法正常下单。查询后台日志，可以看到大量的 duplicate class attempt。

```
java.lang.LinkageError-->loader (instance of  org/springframework/boot/loader/LaunchedURLClassLoader): attempted  duplicate class definition for name: "com/order/vo/OrderAndExtendVO$$BeanMapByCGLIB$$e8178b2a"
StackTrace:
org.springframework.cglib.core.CodeGenerationException: java.lang.LinkageError-->loader (instance of  org/springframework/boot/loader/LaunchedURLClassLoader): attempted  duplicate class definition for name: "com/order/vo/OrderAndExtendVO$$BeanMapByCGLIB$$e8178b2a"
	at org.springframework.cglib.core.ReflectUtils.defineClass(ReflectUtils.java:538)
	at org.springframework.cglib.core.AbstractClassGenerator.generate(AbstractClassGenerator.java:363)
	at org.springframework.cglib.core.AbstractClassGenerator$ClassLoaderData$3.apply(AbstractClassGenerator.java:110)
	at org.springframework.cglib.core.AbstractClassGenerator$ClassLoaderData$3.apply(AbstractClassGenerator.java:108)
	at org.springframework.cglib.core.internal.LoadingCache$2.call(LoadingCache.java:54)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at org.springframework.cglib.core.internal.LoadingCache.createEntry(LoadingCache.java:61)
	at org.springframework.cglib.core.internal.LoadingCache.get(LoadingCache.java:34)
	at org.springframework.cglib.core.AbstractClassGenerator$ClassLoaderData.get(AbstractClassGenerator.java:134)
	at org.springframework.cglib.core.AbstractClassGenerator.create(AbstractClassGenerator.java:319)
	at org.springframework.cglib.beans.BeanMap$Generator.create(BeanMap.java:127)
	at org.springframework.cglib.beans.BeanMap.create(BeanMap.java:59)
	····省略其他堆栈
```

# 问题分析

首先，通过堆栈，可以初步判断，报错是 cglib 尝试生成一个已经存在的 class 导致的。

代码中调用了`BeanMap.create(Object)`方法，这个方法会生成动态代理类。我们直接进入到`AbstractClassGenerator.create(Object)`的源码，可以看到，全局缓存里已经有了就不会再次生成，按理来说，代理类并不会重复生成，难道缓存失效了吗？

一开始我怀疑是因为缓存被禁用了。但是吧，这个 useCache 字段只能通过`AbstractClassGenerator.setUseCache(boolean)`方法设置，而整个项目并没有任何地方引用到这个方法，所以，这个假设并不成立。

```java
abstract public class AbstractClassGenerator<T> implements ClassGenerator {
    // 是否使用缓存
    private boolean useCache = true;
    private static final Object NAME_KEY = new Object();
    // 缓存是全局的
    private static final Source SOURCE = new Source(BeanMap.class.getName());
    protected static class Source {
        String name;
        /*
         * 全局缓存，格式为:
         * <blockquote><pre>
         * {
         *      "classLoader1":{
         *          NAME_KEY:["className1","className2"],
         *          "className1":class1,
         *          "className2":class2
         *      },
         *      "classLoader2":{
         *          NAME_KEY:["className2","className3"],
         *          "className3":class3,
         *          "className2":class2
         *      }
         * }
         * zzs001
         * </pre></blockquote>
         */
        Map cache = new WeakHashMap();
        public Source(String name) {
            this.name = name;
        }
    }

    protected Object create(Object key) {
        try {
        	Class gen = null;
        	
            synchronized (source) {
                ClassLoader loader = getClassLoader();
                Map cache2 = null;
                cache2 = (Map)source.cache.get(loader);
                if (cache2 == null) {
                    cache2 = new HashMap();
                    cache2.put(NAME_KEY, new HashSet());
                    source.cache.put(loader, cache2);
                } else if (useCache) {
                    Reference ref = (Reference)cache2.get(key);
                    gen = (Class) (( ref == null ) ? null : ref.get()); 
                }
                if (gen == null) {
                    Object save = CURRENT.get();
                    CURRENT.set(this);
                    try {
                        this.key = key;
                        
                        if (attemptLoad) {
                            try {
                                gen = loader.loadClass(getClassName());
                            } catch (ClassNotFoundException e) {
                                // ignore
                            }
                        }
                        if (gen == null) {
                            byte[] b = strategy.generate(this);
                            String className = ClassNameReader.getClassName(new ClassReader(b));
                            getClassNameCache(loader).add(className);
                            gen = ReflectUtils.defineClass(className, b, loader);
                        }
                       
                        if (useCache) {
                            cache2.put(key, new WeakReference(gen));
                        }
                        return firstInstance(gen);
                    } finally {
                        CURRENT.set(save);
                    }
                }
            }
            return firstInstance(gen);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }
}
```

我突然想到，这个 cglib 只是 spring 内嵌的 cglib，并不是”真正的 cglib“。缓存只是在当前的这个 cglib 生效，如果原生的 cglib 也要创建这个类，是不是就会报错了呢？

通过查看引用，项目里确实存在这种情况：使用了不同的 cglib 来创建同一个类：

![bug_analysis_006.png](https://img2022.cnblogs.com/blog/1731892/202201/1731892-20220124111431623-1073953928.png)

接着我用代码试验了一下，果然出现了同样的报错：

![bug_analysis_007.png](https://img2022.cnblogs.com/blog/1731892/202201/1731892-20220124111445162-933976674.png)

# 问题解决

于是，我们可以给出结论：**使用了spring 和原生两个不同的 cglib 来生成同一个 class，会因为缓存无法共享而出现 duplicate class attempt 的报错**。

知道了原因，解决的办法就非常简单了。只要把 cglib 的导包改成同一个就行了。

修复后，生产再无该类报错，基本证明我们是对的。

# 结语

以上就是这次生产报错的处理过程。这里我有几个疑惑的地方：

1. cglib 判断一个 class 是否存在，为什么不直接检查项目里的 class？却要用缓存这种不可靠的手段？
2. spring 为什么不直接依赖 cglib？而要自己内嵌一个？

最后，感谢阅读，欢迎交流、指正。

> 本文为原创文章，转载请附上原文出处链接：https://www.cnblogs.com/ZhangZiSheng001/p/15838657.html