package edu.hit.fmpmm.domain.neo4j.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data  // 自动生成getter和setter方法
@EqualsAndHashCode(callSuper = false)
@Node(labels = "Abstraction")
public class Abstraction extends edu.hit.fmpmm.domain.neo4j.node.Node implements Serializable {
    @Id
    @GeneratedValue
    private String id;
    @Property("name")  // 映射到Neo4j节点的属性
    private final String name;  // 除了id外的属性均被final修饰，一种构造优化，提高执行效率

    public Abstraction(String name) {
        this.id = null;  // 自动生成
        this.name = name;
    }
    // ** INCOMING和OUTGOING只能写一个，不能两个都写，会报错 java.lang.StackOverflowError
    @Relationship(type = "has_instance", direction = Relationship.Direction.OUTGOING)
    private List<Instance> hasInstances = new ArrayList<>();

    @Relationship(type = "has_capability", direction = Relationship.Direction.OUTGOING)
    private List<Capability> hasCapabilities = new ArrayList<>();

    @Relationship(type = "include", direction = Relationship.Direction.OUTGOING)
    private List<Abstraction> includes = new ArrayList<>();

    @Relationship(type = "installed_on", direction = Relationship.Direction.OUTGOING)
    private List<Abstraction> installedOns = new ArrayList<>();
}
