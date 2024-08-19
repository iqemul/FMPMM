package edu.hit.fmpmm.domain.neo4j.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@org.springframework.data.neo4j.core.schema.Node("Parameter")
public class Parameter extends Node implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;

    public Parameter() {
    }

    public Parameter(String name) {
        this.name = name;
    }

}
