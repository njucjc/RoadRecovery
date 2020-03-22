package nju.ics.Algorithm;

import static java.lang.Math.pow;
import static nju.ics.Entity.NodeSource.*;

import java.util.List;
import nju.ics.Entity.*;

public class DPAlgorithm implements Algorithm {

    /**
     * Road recovery DP algorithm
     * @param graph graph(G(V, E), T(V))
     * @param path input path P
     *
     * @return output path(P*), return null when failed
     */
    public RuntimePath execute(Graph graph, RuntimePath path, List<Double> configs) {

        double modifyCost = configs.get(0); //0.01
        double addCost = 0.1; //configs.get(1); //0.1
        double deleteCost = 4000; //configs.get(2); //1
        double deleteCost2 = 2;
        double deleteEndCost = configs.get(3); //graph.nodes.size() + 1
        // 保证5个加<1个删 (id9710)

        boolean debug = false;

        RuntimePath originalPath = path;
//        Path originalPath = new Path();
//        for (Node node : path.nodeList) {
//            if (originalPath.nodeList.isEmpty() || node != originalPath.nodeList
//                .get(originalPath.nodeList.size() - 1)) {
//                originalPath.nodeList.add(node);
//            }
//        }
        int originalPathSize = originalPath.runtimeNodeList.size();
        double[][] dp = new double[originalPathSize][2];
        RuntimePath[][] dpPath = new RuntimePath[originalPathSize][2];
        double[][] distanceFromDeletedNodesToIJ = new double[originalPathSize][
            originalPathSize];

        double answer = -1;
        RuntimePath answerPath = new RuntimePath();

        boolean not_delete_first = originalPath.runtimeNodeList.get(0).node.type == NodeType.TOLLSTATION;
        boolean not_delete_last =
            originalPath.runtimeNodeList.get(originalPathSize - 1).node.type == NodeType.TOLLSTATION;

//        ArrayList<Node> reasonableNodeList = new ArrayList<Node>();
//
//        for (int i = 0; i < originalPathSize - 1; ++i) {
//            Path path1 = graph.getShortestPath(originalPath.runtimeNodeList.get(i).node, originalPath.runtimeNodeList.get(i + 1).node);
//            if (path1 != null) reasonableNodeList.addAll(path1.nodeList);
//        }

        for (int i = 0; i < originalPathSize; ++i) {
            for (int j = i; j < originalPathSize; ++j) {
                distanceFromDeletedNodesToIJ[i][j] =
                    j - i <= 1 ? 0 : distanceFromNodesToNodes(graph, originalPath.runtimeNodeList, i, j);
                if (debug) {
                    System.out.println("distance delete " + i + " " + j + ": "
                        + distanceFromDeletedNodesToIJ[i][j]);
                }
            }
        }

        for (int i = 0; i < originalPathSize; ++i) {
            for (int flagI = 0; flagI <= 1; ++flagI) {
                // initial dp array
                dp[i][flagI] = (i == 0) ? (modifyCost * flagI) : -1;
                dpPath[i][flagI] = new RuntimePath();
                // nodeI: v_i or T(v_i) controlled by nodeI
                RuntimeNode nodeI = flagI == 0 ? originalPath.runtimeNodeList.get(i)
                    : new RuntimeNode(originalPath.runtimeNodeList.get(i).node.getMutualNode(), null);
                if (nodeI.node == null) {
                    continue;
                }
                dpPath[i][flagI].runtimeNodeList.add(new RuntimeNode(nodeI.node, nodeI.transTime));
                dpPath[i][flagI].runtimeNodeList.get(0).node.source = IDENTIFY;
                for (int flagJ = 0; flagJ <= 1; ++flagJ) {
                    for (int j = i - 1; j >= 0; --j) {
                        // nodeJ: v_j or T(v_j) controlled by flagJ
                        RuntimeNode nodeJ = flagJ == 0 ? originalPath.runtimeNodeList.get(j)
                            : new RuntimeNode(originalPath.runtimeNodeList.get(j).node.getMutualNode(), null);
                        if (nodeJ.node == null) {
                            continue;
                        }
                        // shortest path from nodeJ to nodeI
                        Path shortestPath = graph.getShortestPath(nodeJ.node, nodeI.node);
                        if (shortestPath == null) {
                            continue;
                        }
                        RuntimePath runtimeShortestPath = new RuntimePath(shortestPath, nodeJ, nodeI);
                        if (nodeI.node.equals(nodeJ.node) || (flagI == 1 && flagJ == 1)) { // When i == j and has one IDENTIFY, then IDENTIFY
                            if (flagJ == 1) { // 反转结点
                                runtimeShortestPath.runtimeNodeList.get(0).node.source = MODIFY;
                            }
                            if (flagI == 1) { // 反转结点
                                runtimeShortestPath.runtimeNodeList
                                    .get(runtimeShortestPath.runtimeNodeList.size() - 1).node.source = MODIFY;
                            }
                        }
                        // FIXME: find suitable cost
                        double distance = runtimeShortestPath.getLength();

                        // update method 1: 从 (j, flagJ) 转移到 (i, flagI)，删除 j+1 到 i-1 之间的门架，补上 j 到 i 的最短路
                        if (dp[j][flagJ] != -1) {
                            double result = dp[j][flagJ]
                                + modifyCost * flagI
                                + deleteCost * pow(i - j - 1, 1)
                                + deleteCost2 * distanceFromDeletedNodesToIJ[j][i]
                                + addCost * pow(distance, 1);
//                                + addCost2 * unreasonableNodes(reasonableNodeList, shortestPath);
                            // update
                            if (dp[i][flagI] == -1 || result <= dp[i][flagI]) {
                                dp[i][flagI] = result;
                                dpPath[i][flagI].runtimeNodeList.clear();
                                dpPath[i][flagI].add(dpPath[j][flagJ]);
//                            if (flagJ == 1 && nodeI.equals(nodeJ)) {
//                                dpPath[i][flagI].nodeList
//                                    .remove(dpPath[i][flagI].nodeList.size() - 1);
//                            }
                                dpPath[i][flagI].add(runtimeShortestPath);
                                if (debug) {
                                    dpPath[i][flagI].print(
                                        "dp[" + i + "][" + flagI + "]: " + dp[i][flagI]
                                            + " (from dp[" + j + "][" + flagJ + "])");
                                }
                            }
                        }
                        // update method 2: delete node 0 to j-1, j+1 to i-1
                        if (!not_delete_first) {
                            double result = modifyCost * (flagJ + flagI)
                                + deleteEndCost * j
                                + deleteCost * pow(i - j - 1, 1.2)
                                + deleteCost2 * distanceFromDeletedNodesToIJ[j][i]
                                + addCost * pow(distance, 1.4);
                            if (dp[i][flagI] == -1 || result <= dp[i][flagI]) {
                                dp[i][flagI] = result;
                                dpPath[i][flagI].runtimeNodeList.clear();
                                dpPath[i][flagI].add(runtimeShortestPath);
                                if (debug) {
                                    dpPath[i][flagI].print(
                                        "dp[" + i + "][" + flagI + "]: " + dp[i][flagI]
                                            + " (from node[" + j + "][" + flagJ + "])");
                                }
                            }
                        }
                    }
                }
                if ((!not_delete_last || i == originalPathSize - 1) && (answer == -1
                    || dp[i][flagI] + (originalPathSize - 1 - i) * deleteEndCost < answer)) {
                    answer = dp[i][flagI] + (originalPathSize - 1 - i) * deleteEndCost; // 相当于后面都删掉
                    answerPath = dpPath[i][flagI];
                }
            }
        }
        return answerPath; // empty when failed
    }

    /***
     * NOTE:
     * any shortest path computed in this method is not accessible from outside,
     * therefore no need to wrap with runtime.
     ***/
    private double distanceFromNodesToNodes(Graph graph, List<RuntimeNode> nodeList, int i, int j) {
        long ret = 0;
        RuntimeNode nodeI = nodeList.get(i);
        RuntimeNode nodeJ = nodeList.get(j);
        for (int k = i + 1; k < j; ++k) {
            long dis = -1;
            RuntimeNode nodeK = nodeList.get(k);
            Path path = graph.getShortestPath(nodeI.node, nodeK.node);
            if (path != null) dis = path.getLength();
            path = graph.getShortestPath(nodeK.node, nodeJ.node);
            if (path != null && (dis == -1 || path.getLength() < dis)) dis = path.getLength();
            if (nodeK.node.getMutualNode() != null) {
                nodeK = new RuntimeNode(nodeK.node.getMutualNode(), null);
                path = graph.getShortestPath(nodeI.node, nodeK.node);
                if (path != null && (dis == -1 || path.getLength() < dis)) dis = path.getLength();
                path = graph.getShortestPath(nodeK.node, nodeJ.node);
                if (path != null && (dis == -1 || path.getLength() < dis)) dis = path.getLength();
            }
            ret += dis;
        }
        return ((double) ret) / (j - i - 1);

//        int ret = -1;
//        for (Node node1 : list1) {
//            for (Node node2 : list2) {
//                Path path = graph.getShortestPath(node1, node2);
//                if (path != null && (ret == -1 || path.getLength() < ret)) {
//                    ret = path.getLength();
//                }
//                path = graph.getShortestPath(node2, node1);
//                if (path != null && (ret == -1 || path.getLength() < ret)) {
//                    ret = path.getLength();
//                }
//            }
//        }
//        return max(ret - 1, 0);
    }

    private int unreasonableNodes(List<Node> reasonableNodeList, Path path) {
        int ret = 0;
        for (Node node: path.nodeList) {
            if (!reasonableNodeList.contains(node)) ret++;
        }
        return ret;
    }
}
