package nju.ics.Entity;

import static nju.ics.Entity.NodeSource.*;
import static nju.ics.Entity.NodeType.TOLLSTATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph {
    //attributes
    public ArrayList<Node> nodes = new ArrayList<Node>();
    public Set<Edge> edgeSet = new HashSet<Edge>();
    public int[][] dist;
    public int[][] pre_node;
    private ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>(); // for Dijkstra

    private static class NodeDijkstra {

        private int dis;
        private int index, pre_node;

        NodeDijkstra(int index, int dis, int pre_node) {
            this.index = index;
            this.dis = dis;
            this.pre_node = pre_node;
        }
    }

    //If inNode equals to outNode, then return only one node
    //If can not find path, then return null
    public Path getShortestPath(Node inNode, Node outNode) {

        //Dijkstra algorithm
        int from = nodes.indexOf(inNode);
        int to = nodes.indexOf(outNode);

        if (dist[from][to] == Integer.MAX_VALUE / 2) {
            return null;
        }
        Path path = new Path();
        for (int x = to; x != -1; x = pre_node[from][x]) {
            Node node = (Node) (nodes.get(x)).clone();
            node.source = (x == to || x == from) ? IDENTIFY : ADD;
            path.nodeList.add(node);
        }
        Collections.reverse(path.nodeList);
//        assert (path.getLength() == dist[from][to]);
        return path;
    }


    public void buildAllShortestPathByDijkstra() {
        for (int i = 0; i < nodes.size(); ++i) {
//            System.out.println(nodes.get(i).index + nodes.get(i).name);
            edges.add(new ArrayList<Integer>());
        }
        for (Edge edge : edgeSet) {
//            if (edge.inNode.getMutualNode() != null && edge.inNode.getMutualNode() // 打印能调头的门架
//                .equals(edge.outNode)) {
//                System.out.println("+++" + edge.inNode.index + edge.inNode.name);
//            }
            edges.get(nodes.indexOf(edge.inNode)).add(nodes.indexOf(edge.outNode));
        }

        dist = new int[nodes.size()][nodes.size()];
        pre_node = new int[nodes.size()][nodes.size()];
        PriorityQueue<NodeDijkstra> q = new PriorityQueue<>(Comparator.comparingInt(x -> x.dis));
        for (int from = 0; from < nodes.size(); ++from) {
            Arrays.fill(dist[from], Integer.MAX_VALUE / 2);
            Arrays.fill(pre_node[from], -1);
            q.clear();
            q.add(new NodeDijkstra(from, 0, -1));

            while (!q.isEmpty()) {
                NodeDijkstra x = q.poll();
                if (dist[from][x.index] >= x.dis) {
                    dist[from][x.index] = x.dis;
                    pre_node[from][x.index] = x.pre_node;
                    if (x.index != from && nodes.get(x.index).type == TOLLSTATION) { // 收费站不能再往下转移
                        continue;
                    }
                    for (int y : edges.get(x.index)) {
                        int disy = x.dis + (int)nodes.get(y).mileage;
                        if (dist[from][y] > disy) {
                            q.add(new NodeDijkstra(y, dist[from][y] = disy, x.index));
                        }
                    }
                }
            }
        }
    }
}
