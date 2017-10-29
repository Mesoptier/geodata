package matchingaggregation;

import java.util.*;

public class BipartiteMatchingBottomUp {

    BipartiteGraph graph;

    class BipartiteGraph {

        Grid originalGrid;
        Grid aggregatedGrid;

        List<LeftNode> leftNodes;
        List<RightNode> rightNodes;

        RightNode [][] rightNodesGrid;

        LinkedList<Edge> allEdges;

        PriorityQueue<LeftNode> leftNodeQueueByTranslation;

        Translation [] translations;

        int nrOfTimesToAggregate;
        int nrOfClasses;

        BipartiteGraph(Grid originalGrid, Grid aggregatedGrid)  {
            this.originalGrid = originalGrid;
            this.aggregatedGrid = aggregatedGrid;
            nrOfClasses = originalGrid.getNumberOfClasses();
            nrOfTimesToAggregate = originalGrid.getNumberOfTimesToAggregate();
            finalMapping = new HashMap<>();
            rightNodesGrid = new RightNode[aggregatedGrid.getHeight()][aggregatedGrid.getWidth()];
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

        public void removeAllEdgesComingFromLeftNode(LeftNode satisfiedLeftNode) {
            for (Edge edgeToRemove : satisfiedLeftNode.edges) {
                edgeToRemove.rightNode.removeEdge(edgeToRemove, satisfiedLeftNode.c);
            }
        }


        public void generateEdgesUntilNoMoreMatches() {
            // Keep adding edges to the biparite graph until we get a 4-matching.
//            System.out.println("Number of times to aggregate: " + nrOfTimesToAggregate);
            while (nrOfTimesToAggregate > 0) {
                LeftNode nextLeftNode = leftNodeQueueByTranslation.poll();
                Translation translation = translations[nextLeftNode.nextTranslation];
//                System.out.println("Current leftnode: " + nextLeftNode.x + ", " + nextLeftNode.y);
//                System.out.println("Translation: " + translation.dx + ", " + translation.dy);
                nextLeftNode.nextTranslation++;
                // Easy trick to get to the nearest node.

                int nextRightNodeX = ((nextLeftNode.x - nextLeftNode.x%2)+ 2*translation.dx)/2 ;
                int nextRightNodeY = ((nextLeftNode.y - nextLeftNode.y%2)+ 2*translation.dy)/2 ;

//                System.out.println("Next rightnode: "+ nextRightNodeX + ", " + nextRightNodeY);
                boolean addToQueueAgain = true;
                if (nextRightNodeX >= 0 && nextRightNodeY >= 0
                        && nextRightNodeX < aggregatedGrid.getWidth() && nextRightNodeY < aggregatedGrid.getHeight())
                {
                    RightNode nextRightNode = rightNodesGrid[nextRightNodeY][nextRightNodeX];
                    if (nextRightNode.classWhichHasBeenSatisfied == -1)
                    {
                        Edge edge = new Edge(nextLeftNode, nextRightNode);
                        nextRightNode.addEdge(edge, nextLeftNode.c);
                        nextLeftNode.addEdge(edge);
                    }

                    // Check if a 4-matching has been satisfied.
                    if (nextRightNode.fourMatchingSatisfied()) {
                        completeMatching(nextRightNode);
                        addToQueueAgain = false;
                    }
                }

                if (addToQueueAgain) {
                    leftNodeQueueByTranslation.add(nextLeftNode);
                }
            }
        }

        private void completeMatching(RightNode satisfiedRightNode) {
            int satisfiedClass = satisfiedRightNode.getClassWhichHasBeenSatisfied();
            TreeSet<Edge> selectedEdges = satisfiedRightNode.getEdgesByClass(satisfiedClass);
//                    System.out.println(selectedEdges.first());
            ArrayList<LeftNode> selectedLeftNodes = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                Edge selectedEdge =  selectedEdges.pollFirst();
//                System.out.println(selectedEdge);
                LeftNode satisfiedLeftNode = selectedEdge.leftNode;
                selectedLeftNodes.add(satisfiedLeftNode);
                finalScore += selectedEdge.euclideanDistanceSquared;

                // Removing the edges from all right nodes.
                removeAllEdgesComingFromLeftNode(satisfiedLeftNode);
                leftNodes.remove(satisfiedLeftNode);
                leftNodeQueueByTranslation.remove(satisfiedLeftNode);
            }

            finalMapping.put(satisfiedRightNode, selectedLeftNodes);

            aggregatedGrid.setYX(satisfiedRightNode.y, satisfiedRightNode.x, satisfiedClass);

            rightNodes.remove(satisfiedRightNode);

            nrOfTimesToAggregate--;

        }

        class LeftNode implements Comparable<LeftNode> {
            int x;
            int y;
            int c; // Class
            int nextTranslation;

            TreeSet<Edge> edges = new TreeSet<>();

            LeftNode(int x, int y, int c) {
                this.x = x;
                this.y = y;
                this.c = c;
                this.nextTranslation = 0;
            }

            void addEdge(Edge edge) {
                edges.add(edge);
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
        }

        class RightNode implements Comparable<RightNode> {
            int x, y;
            TreeSet<Edge> [] edgesByClass;
            int [] classCounts;
            int classWhichHasBeenSatisfied = -1;

            RightNode(int x, int y, int nrOfClasses) {
                this.x = x;
                this.y = y;
                edgesByClass = new TreeSet[nrOfClasses];
                for (int i = 0; i < nrOfClasses; i++) {
                    edgesByClass[i] = new TreeSet<>();
                }
                classCounts = new int[nrOfClasses];
            }

            public TreeSet getEdgesByClass(int c) {
                return edgesByClass[c-1];
            }

            public void addEdge(Edge edge, int c) {
                edgesByClass[c-1].add(edge);
                classCounts[c-1] += 1;
            }

            public void removeEdge(Edge edge, int c) {
                edgesByClass[c-1].remove(edge);
                classCounts[c-1] -= 1;
            }

            public boolean fourMatchingSatisfied() {

                for (int i = 0; i < classCounts.length; i++) {
                    if (classCounts[i] >= 4) {
                        classWhichHasBeenSatisfied = i+1;
                        return true;
                    }
                }
                return false;
            }

            public int getClassWhichHasBeenSatisfied() {
                return classWhichHasBeenSatisfied;
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
        graph.generateEdgesUntilNoMoreMatches();
        timeTaken = System.nanoTime()-startTime;

        System.out.println("Matching time taken = " + timeTaken/1000000 + " ms");


        return aggregatedGrid;
    }








}
