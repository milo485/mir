import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;



class createIndex{
    public static void main(String[] args){
	try{

	IndexWriter indexWriter = new IndexWriter("/tmp/index/", new StandardAnalyzer(), true);

	indexWriter.close();
	//and make it owned by correct user?
	}
	catch (Exception e){
	    System.out.println(e.toString());
	}
    }
}
