import java.util.ArrayList;
import java.util.List;

public class Combination {
	String id;
	String resultPath;
	List<String> jsonList;

	public Combination() {
		this.id = new String();
		this.resultPath = new String();
		this.jsonList = new ArrayList<String>();
	}

	public Combination(String id, List<String> inputList) {
		this.id = id;
		this.resultPath = new String();
		this.jsonList = inputList;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResultPath() {
		return resultPath;
	}

	public void setResultPath(String resultPath) {
		this.resultPath = resultPath;
	}

	public List<String> getInputList() {
		return jsonList;
	}

	public void setInputList(List<String> inputList) {
		this.jsonList = inputList;
	}
	
//	public List<String> getTxtFileNames(){
//		return jsonList.stream().map(FilenameUtils::removeExtension).map(s->s+".txt").collect(Collectors.toList());
//	}

}
