# 关于业务通用框架的思考

业务系统是千差万别的，例如，保存、更新和删除订单，或者保存订单和保存客户，走的根本不是一个流程。但是，它们还是有共同点，它们的流程大致可以分成下面的几个部分：

![zzs_entitybuilder_01](https://img2020.cnblogs.com/blog/1731892/202103/1731892-20210303095019879-1573233082.png)

1. 拿到增删改等操作所需的基础数据；
2. 初始化基础数据；
2. 对基础数据进行校验；
3. 利用基础数据，构建出要进行增删改等操作的对象；
4. 持久化或其他操作。

基于这一点，我试着抽取出一套适用于不同业务、不同用例、不同场景的通用业务框架。刚好，去年部门开始重构订单系统，我试着将自己的想法付诸行动。经过几次调整后，总算形成了一个简单的业务通用框架--entitybuilder。

当然，**我更多想表达的，是一种思想、一种规范，而非工具本身**。如果真要说是框架，entitybuilder 就太简陋了。

# entitybuilder 的结构

entitybuilder 包含三个主要部分，基础数据 base data、构建器 entity builder 和结果对象 result entity。我拿到了  base data，把它丢进 entity builder，entity builder 就会帮我构建出 result entity，拿到 result entity 后，我要持久化也行，直接返回给更上层调用者也行。

entity builder 构建 result entity 的过程被定义为：初始化->校验->构建。

![zzs_entitybuilder_02](https://img2020.cnblogs.com/blog/1731892/202103/1731892-20210303095042938-1036644724.png)

# entitybuilder1.0--规范流程

基于上面的模型，也就有了 entitybuilder1.0，它的结构如下。

对调用者来说，只需要设置好基础数据，调用`build`方法就能完成初始化、校验、构建，当然，`EntityBuilder`还支持仅作为校验器使用，因为有时我们并不需要结果对象，只需要校验基础数据就行。

对实现者来说，用户需要继承`AbstractEntityBuilder`，并实现初始化、校验和构建方法。 

![zzs_entitybuilder_03](https://img2020.cnblogs.com/blog/1731892/202103/1731892-20210303095058767-1600044341.png)

以订单保存为例，下面展示如何使用 entitybuilder。代码的调用非常简单，这里需要注意，`EntityBuilder`对象必须是多例的。

```java
    public String save(DefaultOrderSaveCmd cmd) {
        // 获取构建器（多例的）
        EntityBuilder<BaseOrderSaveCmd, OrderSaveE> entityBuilder = getSaveEntityBuilder();

        // 设置基础数据
        entityBuilder.setBaseData(cmd);
        
        // 构建保存实体
        OrderSaveE entity = entityBuilder.build();
        
        // 持久化操作
        orderDao.save(entity);
        
        return entity.getId();
    }
```

# entitybuilder2.0--多场景支持

entitybuilder1.0 只是规范了业务流程，在多场景方面还是存在问题。

一个业务用例可能会有不同的场景，例如，客户保存可能就不只一个入口，按照 entitybuilder1.0 的设计，我们需要将所有场景的逻辑都堆积到构建器中。显然，这是不合理的。

参考 spring 的 postprocessor，我在构建器中引入了校验器和处理器的支持。构建器中定义了用例的主流程，不同场景可以通过注册校验器和处理器来对主流程进行修饰。在 entitybuilder1.0 的基础上修改，得到以下结构：

![zzs_entitybuilder_05](https://img2020.cnblogs.com/blog/1731892/202103/1731892-20210303095532841-1272571365.png)

和 entitybuilder 1.0 相比，对调用者来说，可以通过注册验器和处理器来影响构建器的主流程，对构建器实现者来说，改为继承`AbstractFlexibleEntityBuilder`。

那么校验器和处理器如何影响主流程呢？下面通过一张图来说明。在 entitybuilder1.0 中，调用者无需知道构建器中的逻辑，现在却需要知道（有好有坏吧）。

![zzs_entitybuilder_04](https://img2020.cnblogs.com/blog/1731892/202103/1731892-20210303095133790-172422034.png)

以订单保存为例，代码示例如下。

```java
    public String save(DefaultOrderSaveCmd cmd) {
        // 获取构建器
        AbstractFlexibleEntityBuilder<BaseOrderSaveCmd, OrderSaveE> entityBuilder = getSaveEntityBuilder();
        
        // 设置基础数据
        entityBuilder.setBaseData(cmd);
        
        // 对构建器进行部分更改，例如注册处理器或检验器
        entityBuilder.registerValidator(myOrderSaveValidator);
        entityBuilder.registerEntityBuilderPostProcessor(myOrderSavePostProcessor);
        
        // 构建保存实体
        OrderSaveE entity = entityBuilder.build();
        
        // 持久化操作
        orderDao.save(entity);
        
        return entity.getId();
    }
```

# 基础数据的组成

entitybuilder 的可用性极大依赖于基础数据的规范。**在 entitybuilder 中，基础数据的成员属性应该包含两个部分：主体属性和关联对象**。例如，订单的基础数据就包括了订单本身以及它的关联对象，如客户、操作人等。

![zzs_entitybuilder_06](https://img2020.cnblogs.com/blog/1731892/202103/1731892-20210303095150232-17188069.png)

为什么要包含这两个部分呢?

构建器中包含了业务的大部分逻辑，我们需要调用各种通用方法，这些方法的入参对象无非就是主体属性或关联对象属性。基础数据对象将在 EntityBuilder 的整个生命周期中传递，通过它来传递关联对象，可以保证关联对象只需要初始化一次，从而减少重复 IO。

如果一开始放入构建器的基础数据对象中已经有关联对象了，那么，构建器也不会再去初始化它。这一点在批量构建时将非常有用。

# 事务控制

在 entitybuilder 的规范中，结果对象的持久化是在一个事务/方法中完成主体对象和关联对象的持久化，但是，在某个场景下，我们需要在事务中进行某些自定义操作，例如，订单保存完成后，向某个外部系统推送数据，推送失败，事务跟着回滚。

针对这种场景，也是可以支持的。事务中的自定义操作将作为函数的形式传递，在基础数据中设置好，持久化时就会执行它。

```java
    @Override
    public String save(DefaultOrderSaveCmd cmd) {
        // 获取构建器
        AbstractFlexibleEntityBuilder<BaseOrderSaveCmd, OrderSaveE> entityBuilder = getSaveEntityBuilder();
        
        // 设置基础数据
        entityBuilder.setBaseData(cmd);
        
        // 对基础数据进行部分更改，例如设置保存事务中需要进行的操作
        cmd.addSaveConsumer(order -> {
            // 有的自定义操作需要放入保存事务，如果失败，订单数据也会回滚
            // 省略代码······
        });

        // 构建保存实体
        OrderSaveE entity = entityBuilder.build();
        
        // 持久化操作
        orderDao.save(entity);
        
        return entity.getId();
    }
    // @Transactional
    public String save(OrderSaveE entity) {
        // 保存订单
        // 省略代码······
        
        // 保存产品
        // 省略代码······
        
        
        // 保存附件
        // 省略代码······
        
        
        // 执行保存事务中的函数
        entity.getSaveConsumers().forEach(x -> x.accept(entity));
        
        return entity.getId();
    }
```

以上基本介绍完 entitybuilder。这里还是强调一点，我更多的是想表达一种思想、一种规范，因为作为工具，entitybuilder 还有很多需要改进的地方。

最后，感谢阅读。

# 参考资料

> 相关源码请移步：[https://github.com/ZhangZiSheng001/zzs-code-thought/01-entitybuilder-demo](https://github.com/ZhangZiSheng001/zzs-code-thought/01-entitybuilder-demo)

>本文为原创文章，转载请附上原文出处链接：[https://www.cnblogs.com/ZhangZiSheng001/p/14472782.html](https://www.cnblogs.com/ZhangZiSheng001/p/14472782.html)