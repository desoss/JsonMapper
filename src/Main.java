import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.InstanceDataMultiProvider;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobMLProfilesMap;

public class Main {

	/* EVERY INPUT JSON FILE MUST HAVE THE EXTENSION .json IN ITS NAME */
	private static final String FILE_INPUT_DIR = "#@INPUTFOLDERPATH@#";

	private static final String FILE_TEXT_EXT = ".json";
	private static final String FILE_OUTPUT_DIR = "#@OUTPUTFOLDERPATH@#";

	/*
	 * Inpus JSONs are of public case, force a private case transformation by
	 * deleting unused parameters an initialing useful ones
	 */
	private static final boolean CONVERT_TO_PRIVATE = false;
	private static final boolean PRETTIFY_JSON = true;

	/*
	 * If ADD_ML_FEATURES = true, MLProfile are added from .json files contained
	 * in FILE_ML_DIR. Every external .json file (containing the MLFeatures)
	 * should named with ApplicationID_. For example if Application id is ID123,
	 * so the file should be named ID123_.json
	 */
	private static final boolean ADD_ML_FEATURES = true;

	private static final String FILE_ML_DIR = "#@MLFOLDERPATH@#";

	static ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
	private String[] list;

	public static void main(String[] args) {
		Main findExt = new Main();
		checkInputs();
		findExt.listFile(FILE_INPUT_DIR, FILE_TEXT_EXT);
		if (PRETTIFY_JSON) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}

		try {
			findExt.convertJSONs(ADD_ML_FEATURES, FILE_ML_DIR);
		} catch (IOException e) {
			System.out.println("Error with JSONs serialization!");
		}
	}

	private static void checkInputs() {
		if (new File(FILE_INPUT_DIR).isDirectory() == false) {
			System.out.println("Directory does not exists : " + FILE_INPUT_DIR);
			System.exit(0);
		}

		if (new File(FILE_OUTPUT_DIR).isDirectory() == false) {
			System.out.println("Directory does not exists : " + FILE_OUTPUT_DIR);
			System.exit(0);
			;
		}
		if (ADD_ML_FEATURES) {
			if (new File(FILE_ML_DIR).isDirectory() == false) {
				System.out.println("Directory does not exists : " + FILE_ML_DIR);
				System.exit(0);
				;
			}
		}
	}

	public void listFile(String folder, String ext) {

		GenericExtFilter filter = new GenericExtFilter(ext);

		File dirInput = new File(folder);

		// list out all the file name and filter by the extension
		String[] list = dirInput.list(filter);

		if (list.length == 0) {
			System.out.println("no files end with : " + ext);
			return;
		}

		for (String file : list) {
			String temp = new StringBuffer(FILE_INPUT_DIR).append(File.separator).append(file).toString();
			System.out.println("file : " + temp);
		}

		this.list = list;

		return;
	}

	/*-----------------------JSON-------------------------*/
	private void convertJSONs(boolean addML, String mlDirecotoryPath)
			throws JsonParseException, JsonMappingException, IOException {
		if (list.length == 0) {
			System.out.println("No JSONs to be converted");
			return;
		}
		Map<String, InstanceData> istanceDataList = new HashMap<>();
		List<InstanceDataMultiProvider> istanceDataMultiProviderList = new ArrayList<>();
		for (String jsonPath : list) {
			InstanceData id = getJsonFromPath(jsonPath);
			if (addML) {
				String mlFileName = jsonPath.split("_")[0] + ".json";
				File f = new File(FILE_ML_DIR + "/" + mlFileName);
				if (f.exists() && !f.isDirectory()) {
					id.setMapJobMLProfiles(getMLProfileFromPath(mlFileName));
				} else {
					System.out
							.println("Not possible to add mlProfile. File not found:" + FILE_ML_DIR + "/" + mlFileName);
				}
			} else {
				System.out.println("MLProfiles Not added from external directory");
			}
			istanceDataList.put(jsonPath, id);
		}

		for (Map.Entry<String, InstanceData> input : istanceDataList.entrySet()) {
			InstanceDataMultiProvider idmp = JsonMapper.ConvertJson(input.getValue(), CONVERT_TO_PRIVATE);
			mapper.writeValue(new File(FILE_OUTPUT_DIR + "/" + input.getKey()), idmp);
			istanceDataMultiProviderList.add(idmp);
		}

	}

	private InstanceData getJsonFromPath(String path) throws JsonParseException, JsonMappingException, IOException {
		File file = new File(FILE_INPUT_DIR + "/" + path);
		InstanceData instanceData = mapper.readValue(file, InstanceData.class);
		return instanceData;
	}

	private JobMLProfilesMap getMLProfileFromPath(String path)
			throws JsonParseException, JsonMappingException, IOException {
		File file = new File(FILE_ML_DIR + "/" + path);
		JobMLProfilesMap mlProfiles = mapper.readValue(file, JobMLProfilesMap.class);
		return mlProfiles;
	}

	/*------------------------------------------------*/

	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}

}
