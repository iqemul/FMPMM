package edu.hit.fmpmm.domain.neo4j.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@org.springframework.data.neo4j.core.schema.Node("Instance")
public class Instance extends Node implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;
    // Instance类型的节点用来表示不同概念的实例时属性不同
    /**
     * 映射除了name的其他属性，这些属性必须以”pro_“作为前缀
     */
    @CompositeProperty(prefix = "pro", delimiter = "_")
    private Map<String, Object> otherProperties = new HashMap<>();

    public Instance() {
    }

    public Instance(String name, Map<String, Object> otherProperties) {
        this.name = name;
        this.otherProperties = otherProperties;
    }

    @Relationship(type = "achieve_goal", direction = Relationship.Direction.OUTGOING)
    private List<Goal> achieveGoals = new ArrayList<>();

    @Relationship(type = "has_capability", direction = Relationship.Direction.OUTGOING)
    private List<Instance> hasCapabilities = new ArrayList<>();

    @Relationship(type = "include", direction = Relationship.Direction.OUTGOING)
    private List<Instance> includes = new ArrayList<>();

    @Relationship(type = "installed_on", direction = Relationship.Direction.OUTGOING)
    private List<Instance> installedOns = new ArrayList<>();
}


