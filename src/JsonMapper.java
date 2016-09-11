
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.JobClass;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.Profile;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.TypeVM;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.TypeVMJobClassKey;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.ClassParameters;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.ClassParametersMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.InstanceDataMultiProvider;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobMLProfile;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobMLProfilesMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobProfile;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobProfilesMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PrivateCloudParameters;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PublicCloudParameters;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PublicCloudParametersMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.VMConfiguration;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.VMConfigurationsMap;

public class JsonMapper {

	public static InstanceDataMultiProvider ConvertJson(InstanceData input,InstanceDataMultiProvider output, boolean convertToPrivate) {

		output.setId(input.getId());

		if (input.getLstClass() != null && !input.getLstClass().isEmpty()) {
			if(output.getMapClassParameters() == null){
				output.setMapClassParameters(new ClassParametersMap());
			}
			output.setMapClassParameters(retrieveMapClassParameters(output.getMapClassParameters().getMapClassParameters(),input.getLstClass()));
		}
		if (input.getMapJobMLProfiles() != null && input.getMapJobMLProfiles().getMapJobMLProfile() != null) {
			if (input.getMapJobMLProfiles().validate()) {
				if(output.getMapJobMLProfiles() == null){
					output.setMapJobMLProfiles(new JobMLProfilesMap());
				}
				output.setMapJobMLProfiles(retrieveMapJobMLProfiles(output.getMapJobMLProfiles().getMapJobMLProfile(),input.getMapJobMLProfiles()));
			} else {
				System.out.println("MLProfile not valid!");
			}
		} else {
			System.out.println("- JobMlProfilesNotFound");
		}
		if (input.getMapProfiles() != null && !input.getMapProfiles().isEmpty()) {
			if(output.getMapJobProfiles() == null){
				output.setMapJobProfiles(new JobProfilesMap());
			}
			output.setMapJobProfiles(retrieveMapJobProfiles(output.getMapJobProfiles().getMapJobProfile(),input.getMapProfiles(), input.getProvider()));
		}
		if (input.getMapTypeVMs() != null && input.getMapTypeVMs().isPresent() && !input.getMapTypeVMs().get().isEmpty()
				&& !convertToPrivate) {
			if(output.getMapPublicCloudParameters() == null){
				output.setMapPublicCloudParameters(new PublicCloudParametersMap());
			}
			output.setMapPublicCloudParameters(
					retrieveMapPublicCloudParameters(output.getMapPublicCloudParameters().getMapPublicCloudParameters(),input.getMapTypeVMs(), input.getProvider()));
		}
		if (input.getMapVMConfigurations() != null && input.getMapVMConfigurations().isPresent()
				&& !input.getMapVMConfigurations().get().getMapVMConfigurations().isEmpty()
				&& input.getMapVMConfigurations().get().getMapVMConfigurations() != null
				&& input.getMapVMConfigurations().get().validate()) {
			output.setMapVMConfigurations(retrieveMapVMConfigurations(output.getMapVMConfigurations().getMapVMConfigurations(),input.getMapVMConfigurations()));
		}
		if (input.getPrivateCloudParameters() != null && input.getPrivateCloudParameters().isPresent()
				&& input.getPrivateCloudParameters().get().validate()) {
			output.setPrivateCloudParameters(retrievePrivateCloudParameters(input.getPrivateCloudParameters()));
		}
		if (convertToPrivate) {
			initializeMissingPrivateParameters(output);
		}
		return output;
	}
	
	public static InstanceDataMultiProvider CombineJsons(Map<String,InstanceData> inputMap, InstanceDataMultiProvider output, boolean convertToPrivate) {
		for(Map.Entry<String, InstanceData> input : inputMap.entrySet()){
			ConvertJson(input.getValue(), output, convertToPrivate);
		}
		return output;
	}

	private static ClassParametersMap retrieveMapClassParameters(Map<String,ClassParameters> mapClassParameters,List<JobClass> lstClass) {
		for (JobClass jobClass : lstClass) {
			ClassParameters cp = new ClassParameters();
			cp.setD(jobClass.getD());
			cp.setHlow(jobClass.getHlow());
			cp.setHup(jobClass.getHup());
			cp.setPenalty(jobClass.getJob_penalty());
			cp.setThink(jobClass.getThink());
			cp.setM(jobClass.getM());
			cp.setV(jobClass.getV());
			if(mapClassParameters.containsKey(jobClass.getId())){
					System.out.println("Multiple ClassParameters with ID: "+jobClass.getId());
			}else{
				mapClassParameters.put(jobClass.getId(), cp);
			}
		}
		return new ClassParametersMap(mapClassParameters);
	}

	private static JobProfilesMap retrieveMapJobProfiles(Map<String, Map<String, Map<String, JobProfile>>> mapJobProfiles, Map<TypeVMJobClassKey, Profile> mapProfiles, String provider) {
		if(mapJobProfiles==null){
			mapJobProfiles = new HashMap<>();
		}
		for (Map.Entry<TypeVMJobClassKey, Profile> entry : mapProfiles.entrySet()) {
			JobProfile p = new JobProfile();

			p.setNm(entry.getValue().getNm());
			p.setNr(entry.getValue().getNr());
			p.setCm(entry.getValue().getCm());
			p.setCr(entry.getValue().getCr());
			p.setMavg(entry.getValue().getMavg());
			p.setMmax(entry.getValue().getMmax());
			p.setRavg(entry.getValue().getRavg());
			p.setRmax(entry.getValue().getRmax());
			p.setSh1max(entry.getValue().getSh1max());
			p.setShtypavg(entry.getValue().getShtypavg());
			p.setShtypmax(entry.getValue().getShtypmax());

			if (mapJobProfiles.containsKey(entry.getKey().getJob())) {
				if (mapJobProfiles.get(entry.getKey().getJob()).containsKey(provider)) {
					if(mapJobProfiles.get(entry.getKey().getJob()).get(provider).containsKey(entry.getKey().getTypeVM())){
						System.out.println("Duplicated entry for mapJobProfiles with JobId:"+entry.getKey().getJob()+" provider:"+provider+" typeVM:"+entry.getKey().getTypeVM());
					}else{
						mapJobProfiles.get(entry.getKey().getJob()).get(provider).put(entry.getKey().getTypeVM(), p);
					}
				} else {
					Map<String, JobProfile> typeVMMap = new HashMap<>();
					typeVMMap.put(entry.getKey().getTypeVM(), p);
					mapJobProfiles.get(entry.getKey().getJob()).put(provider, typeVMMap);
				}
			} else {
				Map<String, Map<String, JobProfile>> providerMap = new HashMap<>();
				Map<String, JobProfile> typeVMMap = new HashMap<>();
				typeVMMap.put(entry.getKey().getTypeVM(), p);
				providerMap.put(provider, typeVMMap);
				mapJobProfiles.put(entry.getKey().getJob(), providerMap);
			}
		}
		return new JobProfilesMap(mapJobProfiles);
	}

	private static PublicCloudParametersMap retrieveMapPublicCloudParameters(
			Map<String, Map<String, Map<String, PublicCloudParameters>>> mapPublicCloudParameters, Optional<Map<String, List<TypeVM>>> mapTypeVMsO, String provider) {
		Map<String, List<TypeVM>> mapTypeVMs = mapTypeVMsO.get();

		if(mapPublicCloudParameters==null){
			mapPublicCloudParameters = new HashMap<>();
		}
		
		for (Map.Entry<String, List<TypeVM>> mapEntry : mapTypeVMs.entrySet()) {
			for (TypeVM lstEntry : mapEntry.getValue()) {

				PublicCloudParameters p = new PublicCloudParameters();
				p.setEta(lstEntry.getEta());
				p.setR(lstEntry.getR());

				if (mapPublicCloudParameters.containsKey(mapEntry.getKey())) {
					if (mapPublicCloudParameters.get(mapEntry.getKey()).containsKey(provider)) {
						if(mapPublicCloudParameters.get(mapEntry.getKey()).get(provider).containsKey(lstEntry.getId())){
							System.out.println("Duplicated entry for mapJobProfiles with JobId:"+mapEntry.getKey()+" provider:"+provider+" typeVM:"+lstEntry.getId());
						}else{
							mapPublicCloudParameters.get(mapEntry.getKey()).get(provider).put(lstEntry.getId(), p);
						}
						
					} else {
						Map<String, PublicCloudParameters> typeVMMap = new HashMap<>();
						typeVMMap.put(lstEntry.getId(), p);
						mapPublicCloudParameters.get(mapEntry.getKey()).put(provider, typeVMMap);
					}
				} else {
					Map<String, Map<String, PublicCloudParameters>> providerMap = new HashMap<>();
					Map<String, PublicCloudParameters> typeVMMap = new HashMap<>();
					typeVMMap.put(lstEntry.getId(), p);
					providerMap.put(provider, typeVMMap);
					mapPublicCloudParameters.put(mapEntry.getKey(), providerMap);
				}
			}

		}
		return new PublicCloudParametersMap(mapPublicCloudParameters);
	}

	private static VMConfigurationsMap retrieveMapVMConfigurations(Map<String, VMConfiguration> map, Optional<VMConfigurationsMap> mapVMConfigurationsO) {
		if(map==null){
			map = new HashMap<>();
		}
		for(Map.Entry<String, VMConfiguration> entry : mapVMConfigurationsO.get().getMapVMConfigurations().entrySet()){
			if(map.containsKey(entry.getKey())){
				System.out.println("Duplicated entry for VMConfigurationsMap with id:"+entry.getKey());
			}else{
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return mapVMConfigurationsO.get();
	}

	private static PrivateCloudParameters retrievePrivateCloudParameters(
			Optional<PrivateCloudParameters> privateCloudParameters) {
		return privateCloudParameters.get();
	}

	private static JobMLProfilesMap retrieveMapJobMLProfiles(Map<String, JobMLProfile> map, JobMLProfilesMap mapJobMLProfiles) {
		for(Map.Entry<String, JobMLProfile> entry : mapJobMLProfiles.getMapJobMLProfile().entrySet()){
			if(map.containsKey(entry.getKey())){
				System.out.println("Duplicated entry for JobMLProfilesMap with id:"+entry.getKey());
			}else{
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return new JobMLProfilesMap(map);
	}

	private static void initializeMissingPrivateParameters(InstanceDataMultiProvider input) {
		input.setPrivateCloudParameters(getDefaultPrivateCloudParameters());
		List<String> l = new ArrayList<String>();
		l.addAll(input.getMapJobProfiles().getTypeVMs());
		input.setMapVMConfigurations(getDefaultVMConfiguration(l));
	}

	private static PrivateCloudParameters getDefaultPrivateCloudParameters() {
		PrivateCloudParameters p = new PrivateCloudParameters();
		p.setE(0);
		p.setM(0);
		p.setN(0);
		p.setV(0);
		return p;
	}

	private static VMConfigurationsMap getDefaultVMConfiguration(List<String> lst) {
		Map<String, VMConfiguration> map = new HashMap<>();
		for (String s : lst) {
			VMConfiguration c = new VMConfiguration();
			c.setCore(0);
			c.setMemory(0);
			c.setProvider("inHouse");
			c.setCost(Optional.of(0.0));
			map.put(s, c);
		}
		return new VMConfigurationsMap(map);
	}
}
