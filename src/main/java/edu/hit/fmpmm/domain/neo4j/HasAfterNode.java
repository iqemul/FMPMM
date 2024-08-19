package edu.hit.fmpmm.domain.neo4j;

import java.util.List;

public interface HasAfterNode {  // 用于实现只针对Operation和Action的排序
    List<HasAfterNode> customGetAfters();

    String customGetId();
}
