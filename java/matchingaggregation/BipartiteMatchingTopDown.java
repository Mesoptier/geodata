package matchingaggregation;

import java.util.*;

public class BipartiteMatchingTopDown {

    final int FOUR_MATCHING = 4;

    BipartiteGraph graph;

    class BipartiteGraph {

        Grid originalGrid;
        Grid aggregatedGrid;

        List<LeftNode> leftNodes;
        List<RightNode> rightNodes;

        RightNode [][] rightNodesGrid;

        TreeSet<Edge> allAddedEdges;

        PriorityQueue<LeftNode> leftNodeQueueByTranslation;

        Translation [] translations;

        int nrOfTimesToAggregate;
        int nrOfClasses;
        int [] nrOfSkipsPerClass;

        boolean allLeftNodesAre4Matched;

        BipartiteGraph(Grid originalGrid, Grid aggregatedGrid)  {
            this.originalGrid = originalGrid;
            this.aggregatedGrid = aggregatedGrid;
            nrOfClasses = originalGrid.getNumberOfClasses();
            nrOfTimesToAggregate = originalGrid.getNumberOfTimesToAggregate();
            finalMapping = new HashMap<>();
            rightNodesGrid = new RightNode[aggregatedGrid.getHeight()][aggregatedGrid.getWidth()];
            nrOfSkipsPerClass = originalGrid.getNumberOfTimesToSkipPerClass();
            allAddedEdges = new TreeSet<>();
            initializeTranslations();
            initializeGraph();

        }

        protected void initializeTranslations() {
            leftNodeQueueByTranslation = new PriorityQueue<>((node1, node2) ->
            {
                int compareNextTranslations = Integer.compare(node1.nextTranslation, node2.nextTranslation);
                if (compareNextTranslations != 0) {
                    return compareNextTranslations;
                }
                int xCompare = Integer.compare(node1.x, node2.x);
                if (xCompare != 0) {
                    return xCompare;
                }
                return Integer.compare(node1.y, node2.y);
            });


            translations = new Translation[(originalGrid.width*2+1)*(originalGrid.height*2+1)];
            {
                int i = 0;
                for (int dx = -originalGrid.width; dx <= originalGrid.width; dx++) {
                    for (int dy = -originalGrid.height; dy <= originalGrid.height; dy++) {
                        translations[i] = new Translation(dx, dy);
                        i++;
                    }
                }
            }
            Arrays.sort(translations);
        }

        protected void initializeGraph() {
            // Create the bipartite sets L and R
            // We only have to add the nodes to L which are non-empty.
            leftNodes = new ArrayList<>();
            rightNodes = new ArrayList<>();

            for (int y = 0; y < originalGrid.height; y++) {
                for (int x = 0; x < originalGrid.width; x++) {
                    int c = originalGrid.getYX(y,x);
                    if (c != 0) {
                        LeftNode newLeftNode = new LeftNode(x, y, c);
                        leftNodes.add(newLeftNode);
                        leftNodeQueueByTranslation.add(newLeftNode);
                    }
                }
            }

            for (int y = 0; y < aggregatedGrid.height; y++) {
                for (int x = 0; x < aggregatedGrid.width; x++) {
                    RightNode newRightNode = new RightNode(x, y, nrOfClasses);
                    rightNodes.add(newRightNode);
                    rightNodesGrid[y][x] = newRightNode;
                }
            }


        }

        HashMap<RightNode, ArrayList<LeftNode>> finalMapping;
        public double finalScore;

        public double getFinalScore() {
            return finalScore;
        }

        public HashMap<RightNode,  ArrayList<LeftNode>> getFinalMapping() {
            return getFinalMapping();
        }

        void removeEdge(Edge edge, boolean removeFromAllEdges, boolean removeFromLeftNodeEdges) {
            edge.rightNode.removeRightEdge(edge);
            if (removeFromAllEdges) {
                allAddedEdges.remove(edge);
            }
            if (removeFromLeftNodeEdges) {
                edge.leftNode.edges.remove(edge);
            }
        }


        public void generateEdgesUntilAllLeftNodesAreFourMatched() {
            // Keep adding edges to the biparite graph until all left nodes are 4-matched.
            while (! leftNodeQueueByTranslation.isEmpty()) {
                LeftNode nextLeftNode = leftNodeQueueByTranslation.poll();
                nextLeftNode.isInQueue = false;
//                System.out.println("Current leftnode: " + nextLeftNode.x + ", " + nextLeftNode.y);
//                System.out.println("Translation: " + translation.dx + ", " + translation.dy);

                // Check if it makes sense to add an edge.
                if (!nextLeftNode.hasAlreadyValidMatching() && !nextLeftNode.alreadyAggregated) {
                    Translation translation = translations[nextLeftNode.nextTranslation];
                    nextLeftNode.nextTranslation ++;

                    // Math trick to get to the required node in the aggregated grid.
                    int nextRightNodeX = ((nextLeftNode.x - nextLeftNode.x%2)+ 2*translation.dx)/2 ;
                    int nextRightNodeY = ((nextLeftNode.y - nextLeftNode.y%2)+ 2*translation.dy)/2 ;

                    if (nextRightNodeX >= 0 && nextRightNodeY >= 0
                            && nextRightNodeX < aggregatedGrid.getWidth() && nextRightNodeY < aggregatedGrid.getHeight())
                    {
                        RightNode nextRightNode = rightNodesGrid[nextRightNodeY][nextRightNodeX];
                        if (! nextRightNode.fourMatchingSatisfied())
                        {
                            Edge edge = new Edge(nextLeftNode, nextRightNode);
                            nextRightNode.addEdge(edge);
                            nextLeftNode.addEdge(edge);
                            allAddedEdges.add(edge);
                        }
                    }

                    addLeftNodeToQueueIfNecessary(nextLeftNode);
                }
            }
        }

        private void addLeftNodeToQueueIfNecessary(LeftNode nextLeftNode) {
            if (!nextLeftNode.isInQueue && !nextLeftNode.hasAlreadyValidMatching()) {
                nextLeftNode.isInQueue = true;
                leftNodeQueueByTranslation.add(nextLeftNode);
            }
        }

        private void completeMatching(Edge lastEdge) {
            RightNode satisfiedRightNode = lastEdge.rightNode;
            LeftNode curLeftNode = lastEdge.leftNode;

            ArrayList<LeftNode> selectedLeftNodes = new ArrayList<>();
            selectedLeftNodes.add(curLeftNode);

            finalScore += lastEdge.euclideanDistanceSquared;

            // Selects the three other edges from the right node.
            for (int i = 0; i < FOUR_MATCHING - 1; i++) {
                Edge selectedEdge  = satisfiedRightNode.edgesByClass[curLeftNode.c-1].first();

                LeftNode satisfiedLeftNode = selectedEdge.leftNode;
                selectedLeftNodes.add(satisfiedLeftNode);
                finalScore += selectedEdge.euclideanDistanceSquared;

                removeEdge(selectedEdge, true, true);
            }

            for (TreeSet<Edge> edgesByColor : satisfiedRightNode.edgesByClass) {
                while (!edgesByColor.isEmpty()) {
                    Edge e = edgesByColor.first();
                    removeEdge(e, true, true);
                }
            }


            finalMapping.put(satisfiedRightNode, selectedLeftNodes);

            aggregatedGrid.setYX(satisfiedRightNode.y, satisfiedRightNode.x, lastEdge.leftNode.c);

            satisfiedRightNode.hasBeenSatisfied = true;

            rightNodes.remove(satisfiedRightNode);

            for (LeftNode completedLeftNode : selectedLeftNodes) {
                completedLeftNode.done();
            }

            nrOfTimesToAggregate--;
        }


        public void completeSingleMatching() {
            while (true) {
                if (!leftNodeQueueByTranslation.isEmpty()) {
                    break;
                }
                // Trying to match from the longest edge possible to minimize longest length.
                Edge lastEdge = allAddedEdges.pollLast();
                removeEdge(lastEdge, false, true);
                LeftNode satisfiedLeftNode = lastEdge.leftNode;
//                if (lastEdge.rightNode.edgesByClass[satisfiedLeftNode.c].size() < 4) {
//                    allLeftNodesAre4Matched = false;
//                    break;
//                }
                if (! satisfiedLeftNode.hasAlreadyValidMatching()) {
                    int satisfiedClass = satisfiedLeftNode.c;
                    if (nrOfSkipsPerClass[satisfiedClass-1] > 0) {
                        nrOfSkipsPerClass[satisfiedClass-1] --;
                        satisfiedLeftNode.done();
                    }
                    else {
                        completeMatching(lastEdge);
                    }
                    break;
                }
            }
        }

        class LeftNode implements Comparable<LeftNode> {
            int x;
            int y;
            int c; // Class
            int nextTranslation;
            int nrOfValidFourMatches;
            boolean alreadyAggregated;

            TreeSet<Edge> edges = new TreeSet<>();
            public boolean isInQueue;

            LeftNode(int x, int y, int c) {
                this.x = x;
                this.y = y;
                this.c = c;
                this.nextTranslation = 0;
                alreadyAggregated = false;
                nrOfValidFourMatches = 0;
                isInQueue = false;
            }

            public void addEdge(Edge edge) {
                edges.add(edge);
            }

            public boolean hasAlreadyValidMatching() {
                return nrOfValidFourMatches >= 1;
            }

            public void increaseNrOfMatchings() {
                this.nrOfValidFourMatches ++ ;
            }

            public void decreaseNrOfMatchings() {
                this.nrOfValidFourMatches -- ;
                addLeftNodeToQueueIfNecessary(this);
            }


            @Override
            public int compareTo(LeftNode o) {
                int compareX = Integer.compare(x, o.x);

                if (compareX != 0) {
                    return compareX;
                }
                return Integer.compare(y, o.y);
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof LeftNode)) {
                    return false;
                }
                return compareTo((LeftNode) obj) == 0;
            }

            public void done() {
                alreadyAggregated = true;
                for (Edge edge : edges) {
                    removeEdge(edge, true, false);
                }
                edges.clear();
            }
        }

        class RightNode implements Comparable<RightNode> {
            int x, y;
            public TreeSet<Edge> [] edgesByClass;
            int [] classCounts;
            boolean hasBeenSatisfied;

            RightNode(int x, int y, int nrOfClasses) {
                this.x = x;
                this.y = y;
                edgesByClass = new TreeSet[nrOfClasses];
                for (int i = 0; i < nrOfClasses; i++) {
                    edgesByClass[i] = new TreeSet<>();
                }
                classCounts = new int[nrOfClasses];
                hasBeenSatisfied = false;
            }

            public TreeSet getEdgesByClass(int c) {
                return edgesByClass[c-1];
            }

            public void addEdge(Edge edgeToAdd) {
                int c = edgeToAdd.leftNode.c;
                edgesByClass[c-1].add(edgeToAdd);
                classCounts[c-1] += 1;
                if (edgesByClass[c-1].size() == FOUR_MATCHING) {
                    for (Edge edge : edgesByClass[c-1]) {
                        edge.leftNode.increaseNrOfMatchings();
                    }
//                    hasBeenSatisfied = true;
                }
                else if (edgesByClass[c-1].size() > FOUR_MATCHING) {
                    edgeToAdd.leftNode.increaseNrOfMatchings();
                }

            }

            public void removeRightEdge(Edge edgeToRemove) {
                int c = edgeToRemove.leftNode.c;
                if (edgesByClass[c-1].size() == FOUR_MATCHING) {
                    for (Edge edge : edgesByClass[c-1]) {
                        edge.leftNode.decreaseNrOfMatchings();
                    }
                }
                else if (edgesByClass[c-1].size() > FOUR_MATCHING) {
                    edgeToRemove.leftNode.decreaseNrOfMatchings();
                }
                edgesByClass[c-1].remove(edgeToRemove);
                classCounts[c-1] -= 1;
            }

            public boolean fourMatchingSatisfied() {

                return hasBeenSatisfied;
            }


            @Override
            public int compareTo(RightNode o) {
                int compareX = Integer.compare(x, o.x);

                if (compareX != 0) {
                    return compareX;
                }
                return Integer.compare(y, o.y);
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof RightNode)) {
                    return false;
                }
                return compareTo((RightNode) obj) == 0;
            }

            @Override
            public int hashCode() {
                return Objects.hash(x, y);
            }

            @Override
            public String toString() {
                return "Right (" + x + ", " + y + ")";
            }
        }


        class Edge implements Comparable<Edge> {
            LeftNode leftNode;
            RightNode rightNode;
            double euclideanDistanceSquared;

            Edge(LeftNode leftNode, RightNode rightNode) {
                this.leftNode = leftNode;
                this.rightNode = rightNode;
                double xDist = (2*rightNode.x + 0.5) - leftNode.x;
                double yDist = (2*rightNode.y + 0.5) - leftNode.y;

                this.euclideanDistanceSquared = xDist * xDist + yDist * yDist;
            }

            @Override
            public int compareTo(Edge o) {
                int compareEdgeLength = Double.compare(euclideanDistanceSquared, o.euclideanDistanceSquared);
                if (compareEdgeLength != 0) {
                    return compareEdgeLength;
                }

                int compareLeftNode = leftNode.compareTo(o.leftNode);
                if (compareLeftNode != 0) {
                    return compareLeftNode;
                }

                return rightNode.compareTo(o.rightNode);

            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Edge)) {
                    return false;
                }
                return compareTo((Edge) obj) == 0;
            }

            @Override
            public String toString() {
                return "Edge from L node (" + leftNode.y + ", " + leftNode.x + ", " + leftNode.c + ") " +
                        "to R node (" + rightNode.y + ", " + rightNode.x + ") (y,x). Distance: " + euclideanDistanceSquared;
            }
        }

    }

    class Translation implements Comparable<Translation> {

        int dx, dy;
        double transformedLengthSquared;

        Translation(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
            double transformedDX = dx*2+0.5;
            double transformedDY = dy*2+0.5;
            transformedLengthSquared = transformedDX*transformedDX+transformedDY*transformedDY;
        }

        @Override
        public int compareTo(Translation o) {
            int lengthSquaredCompare = Double.compare(transformedLengthSquared, o.transformedLengthSquared);
            if (lengthSquaredCompare != 0) {
                return lengthSquaredCompare;
            }
            int dxCompare = Integer.compare(dx, o.dx);
            if (dxCompare != 0) {
                return dxCompare;
            }
            return Integer.compare(dy, o.dy);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Translation)) {
                return false;
            }
            return compareTo((Translation) obj) == 0;
        }

        @Override
        public String toString() {
            return "translation ("+dx+", "+dy+") {"+transformedLengthSquared+"}";
        }

    }

    public double getFinalScore() {
        return graph.getFinalScore();
    }

    public HashMap<Node,  ArrayList<Node>> getFinalMapping() {
        // Does not work yet, but easy to fix.
        HashMap<Node,  ArrayList<Node>> finalHashMap = new HashMap<>();

        for (BipartiteGraph.RightNode key : graph.getFinalMapping().keySet()) {
            ArrayList<BipartiteGraph.LeftNode> values = graph.getFinalMapping().get(key);
            ArrayList<Node> leftNodes = new ArrayList<>();
            int classValue = leftNodes.get(0).c;
            values.forEach( (woop) -> {
                Node leftValue = new Node(woop.x, woop.y, woop.c);
                leftNodes.add(leftValue);
            });

            Node rightNode = new Node(key.x, key.y, classValue);
            finalHashMap.put(rightNode, leftNodes);
        }
        return finalHashMap;
    }





    Grid performAggregation(Grid originalGrid) {

        originalGrid = originalGrid.clone();
        Grid paddedGrid = originalGrid.padOddDimensions();

        Grid aggregatedGrid = new Grid(paddedGrid.getHeight() / 2, paddedGrid.getWidth() / 2);

        System.out.println("Creating a bipartite graph...");
        long startTime = System.nanoTime();
        graph = new BipartiteGraph(originalGrid, aggregatedGrid);
        long timeTaken = System.nanoTime()-startTime;
        System.out.println("Graph initialization time taken = " + timeTaken/1000000 + " ms");

        System.out.println("Start matching...");
        startTime = System.nanoTime();

        while (graph.nrOfTimesToAggregate > 0) {
            graph.generateEdgesUntilAllLeftNodesAreFourMatched();
            graph.completeSingleMatching();
        }
        timeTaken = System.nanoTime()-startTime;

        System.out.println("Matching time taken = " + timeTaken/1000000 + " ms");


        return aggregatedGrid;
    }
}
