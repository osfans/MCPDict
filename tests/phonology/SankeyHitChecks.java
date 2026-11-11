import com.osfans.mcpdict.SankeyHit;
public class SankeyHitChecks {
    public static void main(String[] args){
        if(!SankeyHit.contains(100,10,10,5,50,12,1))throw new AssertionError("flat inside");
        if(SankeyHit.contains(100,10,10,5,50,40,1))throw new AssertionError("flat outside");
        if(!SankeyHit.contains(100,0,100,4,49,52,3))throw new AssertionError("curve inside");
        if(SankeyHit.contains(100,0,100,4,1,50,3))throw new AssertionError("outside x");
        if(!SankeyHit.contains(100,100000,100000,.2f,50,100004,5))throw new AssertionError("thin tall chart");
        System.out.println("5 Sankey hit checks passed");
    }
}
