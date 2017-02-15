public class Link {
    public String node;
    public int weight;

    public Link(String _node, int _weight){
        node = _node;
        weight = _weight;
    }

    public String getNode(){
        return node;
    }

    public int getWeight(){
        return weight;
    }

    public String toString(){
        return node + " " + weight;
    }
}
