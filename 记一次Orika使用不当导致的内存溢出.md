# hprof 文件分析

2021-08-24，订单中心的一个项目出现了 OOM 异常，使用 MemoryAnalyzer 打开 dump 出来的 hprof 文件，可以看到 91.27% 的内存被一个超大对象`javassist.ClassPool`占用了。

那么，`ClassPool`是一个什么样的对象呢？我们知道，javassist 可以用来动态生成类，而生成的类就是放在这个`ClassPool`里面，具体以`javassist.CtClass`的形式存在。

所以，初步分析是 OOM 的原因是 javassist 生成的`CtClass`对象过多，即 **javassist 生成了太多的类**。

![bug_analysis_001](https://img2020.cnblogs.com/blog/1731892/202108/1731892-20210825143217250-1765766432.png)

为了验证我的猜想，我需要看看`CtClass`对象的内存情况，点击 Actions -> Histogram，如图。果然，这 2.3 G 的内存就是`CtClass`对象占用的。

![bug_analysis_002](https://img2020.cnblogs.com/blog/1731892/202108/1731892-20210825143234833-1336922419.png)

接下来，我需要知道这些`CtClass`对象都是哪些类，点击 List objects -> with outgoing references。这时可以看到，项目里生成了大量的`Orika_ProductionOrderUpdateCmd_ProductionOrderE_Mapper*`。

看着这些类的命名规则，是不是很熟悉呢？它们都是 orika 映射 bean 时动态生成的类。所以，**大量的`CtClass`对象是由 orika 产生**。orika 的原理我之前讲过（[cglib、orika、spring等bean copy工具性能测试和原理分析](https://www.cnblogs.com/ZhangZiSheng001/p/14108080.html)），这里就不再赘述。

![bug_analysis_003](https://img2020.cnblogs.com/blog/1731892/202108/1731892-20210825143249141-1451740867.png)

但是，**orika 生成的映射类是可以复用的，为什么还会有这么多重复的映射类呢？**

# 项目代码分析

在项目中找到唯一一处将`ProductionOrderE`映射成`ProductionOrderUpdateCmd`的地方。

![bug_analysis_004](https://img2020.cnblogs.com/blog/1731892/202108/1731892-20210825143303810-797090246.png)

在项目中，其他地方都是使用方法 1，唯独这里使用了方法 2，所以，有理由怀疑是不是方法 2 有 bug 呢？

```java
public class BeanUtils {
    // 方法1
    public static <S, D> D copy(S source, Class<D> destinationClass) {
        // ······
    }
    // 方法2
    public static <S, D> D copy(S source, Class<D> destinationClass, String excludeFields) {
        // ······
    }
}
```

于是，我写了个简单的 demo，如下。我的假设是，**使用方法 2 不会复用映射类，每 copy 一次就生成一个映射类，最终导致映射类过多**。至于生成了几个映射类，我们可以通过输出映射类文件的方式来判断，使用启动参数`-Dma.glasnost.orika.GeneratedSourceCode.writeSourceFiles=true -Dma.glasnost.orika.writeSourceFilesToPath=D:/tmp/orika`可以输出映射类文件。

```java
   public static void main(String[] args) {
       ProductionOrderE productionOrder = new ProductionOrderE();
       // 使用方法2
       ProductionOrderUpdateCmd copy = BeanUtils.copy(productionOrder, ProductionOrderUpdateCmd.class, 
               "belongShop,belongOrg,userOperate,orgExtendInfo");
       ProductionOrderUpdateCmd copy2 = BeanUtils.copy(productionOrder, ProductionOrderUpdateCmd.class, 
               "belongShop,belongOrg,userOperate,orgExtendInfo");
       
       // 使用方法1
       // ProductionOrderUpdateCmd copy3 = BeanUtils.copy(productionOrder, ProductionOrderUpdateCmd.class);
       // ProductionOrderUpdateCmd copy4 = BeanUtils.copy(productionOrder, ProductionOrderUpdateCmd.class);
       // zzs001
   }
```

运行方法，我们会发现，使用方法 1 时，只生成了一个映射类，而使用方法 2 时，生成了两个映射类。

![bug_analysis_005](https://img2020.cnblogs.com/blog/1731892/202108/1731892-20210825143319849-1525598382.png)

以下是方法 2 的底层封装，这里使用`ClassMapBuilder`重新配置了`ProductionOrderUpdateCmd`和`ProductionOrderE`的映射关系，导致上一次 copy 时生成的`CtNewClass`对象不再复用。

所以，**在使用 orika 时，A->B 的映射关系只能定义一次，不能反复定义**。

```java
   private MapperFactory mapperFactory; 
   public <S, D> D copy(S source, Type<S> from, Type<D> to, String excludeFields) {
        List<String> list = new ArrayList<>();
        if(excludeFields != null) {
            list = Arrays.asList(excludeFields.split(","));
        }
        ClassMapBuilder cb = this.mapperFactory.classMap(from, to);
        for(String s : list) {
            cb.exclude(s.trim());
        }
        cb.byDefault().register();
        return this.mapperFactory.getMapperFacade().map(source, from, to);
        // zzs001
    }
```

# 解决方案

经过上面的分析，解决方案就呼之欲出了，我们只需要在初始化时一次定义好`ProductionOrderUpdateCmd`和`ProductionOrderE`的映射关系就行了，如下。当然，方法 2 不能再用了。

```java
public class BeanUtils {
    static {
        ClassMapBuilder cb = BeanToolkit.instance().getMapperFactory().classMap(
                TypeFactory.valueOf(ProductionOrderE.class), 
                TypeFactory.valueOf(ProductionOrderUpdateCmd.class)
                );
        cb.exclude("belongShop");
        cb.exclude("belongOrg");
        cb.exclude("userOperate");
        cb.exclude("orgExtendInfo");
        cb.byDefault().register();
        // zzs001
    }
}
```

# 结语

经过以上分析，我们找到了 OOM 的原因，并较好地解决了问题。其实，我们应该更早的监控到异常，像上面说的这种会出现非堆内存过高的情况。

最后，感谢阅读，欢迎私信交流。

> 本文为原创文章，转载请附上原文出处链接：https://www.cnblogs.com/ZhangZiSheng001/p/15184914.html