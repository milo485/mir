package mir.misc;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;



class createIndex{
    public static void main(String[] args){
        try{

        IndexWriter indexWriter = new IndexWriter(args[0], new StandardAnalyzer(), true);

        indexWriter.close();
        //and make it owned by correct user?(not in java!)
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
