package edu.hit.fmpmm.util;

import edu.hit.fmpmm.domain.exception.ExecutionLogicException;
import edu.hit.fmpmm.domain.neo4j.HasAfterNode;
import edu.hit.fmpmm.domain.neo4j.node.Node;

import java.util.*;

public class DataProcessor {
    public List<Node> getIntersection(List<Node> data1, List<Node> data2) {  // 得到两组数据的交集
        Set<Node> tmpRes = new HashSet<>();
        HashMap<Node, Integer> map = new HashMap<>();
        for (Node d : data1) {
            map.put(d, 1);
        }
        for (Node d : data2) {
            int value = map.getOrDefault(d, 0);  // 得到0代表data1中没有
            if (value == 1) {  // 默认输入的两个列表可能存在数据重复
                tmpRes.add(d);
            }
        }
        return new ArrayList<>(tmpRes);
    }

    public List<? extends HasAfterNode> toPoSort(List<? extends HasAfterNode> nodes) {
        // 计算每个节点的入度
        Map<HasAfterNode, Integer> inDegree = new HashMap<>();
        for (HasAfterNode node : nodes) {
            List<HasAfterNode> afterNodes = node.customGetAfters();
            boolean flag = false;
            for (HasAfterNode afterNode : afterNodes) {
                for (HasAfterNode n : nodes) {
                    if (Objects.equals(afterNode.customGetId(), n.customGetId())) {  // afterNode.equals(n)
                        int value = inDegree.getOrDefault(n, 0);
                        inDegree.put(n, value + 1);
                        flag = true;
                        break;  // 能保证afterNodes和nodes的交集只有一个元素
                    }
                }
                if (flag) {
                    break;
                }
            }
            // List<HasAfterNode> intersection = getIntersection(afterNodes, nodes); // 用这个不太好写变量类型啊烦人
        }
        // 将入度为0的节点放入队列，这里必须将Map中加入入度为0的节点
        for (HasAfterNode node : nodes) {
            int value = inDegree.getOrDefault(node, 0);
            inDegree.put(node, value);  // 避免有入度为0的节点不在map中
        }
        Queue<HasAfterNode> queue = new LinkedList<>();
        for (Map.Entry<HasAfterNode, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        List<HasAfterNode> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            HasAfterNode current = queue.poll();
            result.add(current);
            assert current != null;
            List<HasAfterNode> afters = current.customGetAfters();
            boolean flag = false;
            for (HasAfterNode afterNode : afters) {
                for (HasAfterNode n : nodes) {
                    if (afterNode.equals(n)) {
                        int value = inDegree.getOrDefault(n, 0);
                        inDegree.put(n, value - 1);
                        if (value - 1 == 0) {
                            queue.offer(n);
                        }
                        flag = true;
                        break;
                    }
                }
                if (flag) {  // 因为一定能确保待排序的是一个单链表形式，所以才敢提前break
                    break;
                }
            }
        }
        if (result.size() != nodes.size()) {
            throw new ExecutionLogicException("拓扑排序出现错误");
        }
        return result;
    }

    /**
     * 欧拉角转四元数
     *
     * @param euler 欧拉角
     * @return 四元数
     */
    public List<Double> euler2quaternion(List<Double> euler) {
        // 角度转换为弧度
        double[] nums = new double[6];  // cr, sr, cp, sp, cy, sy
        int j = 0;
        for (int i = 0; i < nums.length; i += 2) {
            nums[i] = Math.cos(euler.get(j) * 0.5);
            nums[i + 1] = Math.sin(euler.get(j) * 0.5);
            j++;
        }
        List<Double> quaternion = new ArrayList<>();
        quaternion.add(nums[4] * nums[2] * nums[1] - nums[5] * nums[3] * nums[0]);  // qx
        quaternion.add(nums[5] * nums[2] * nums[1] + nums[4] * nums[3] * nums[0]);  // qy
        quaternion.add(nums[5] * nums[2] * nums[0] - nums[4] * nums[3] * nums[1]);  // qz
        quaternion.add(nums[4] * nums[2] * nums[0] + nums[5] * nums[3] * nums[1]);  // qw
        return quaternion;
    }

    public List<Double> euler2quaternion(List<Double> angle, boolean isDegree) {
        List<Double> eulerRadians;
        // 角度转换为弧度
        if (isDegree) {
            eulerRadians = new ArrayList<>();
            for (Double e : angle) {
                eulerRadians.add(Math.toRadians(e));
            }
        } else {
            eulerRadians = angle;
        }
        return euler2quaternion(eulerRadians);

    }
}
