
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.InstanceData_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.JobClass_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.Profile_old;
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
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.TypeVM;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.TypeVMJobClassKey;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.VMConfiguration;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.VMConfigurationsMap;

public class JsonMapper {

	public static InstanceDataMultiProvider addInstanceDataToOutputJSON(InstanceData_old input,InstanceDataMultiProvider output, boolean convertToPrivate) {
		
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
			convertProvider(output);
		}
		return output;
	}
	
	/**
	 * override providers names in all the right maps 
	 */
	private static void convertProvider(InstanceDataMultiProvider output) {
		for(VMConfiguration entry : output.getMapVMConfigurations().getMapVMConfigurations().values()){
			entry.setProvider(Main.PRIVATE_PROVIDER_NAME);
		}
		Map<String, Map<String, Map<String, JobProfile>>> mapJobIds = new HashMap<>();
		for (Map.Entry<String, Map<String, Map<String, JobProfile>>> jobIDs : output.getMapJobProfiles().getMapJobProfile().entrySet()) {
			Map<String, Map<String, JobProfile>> mapProviders = new HashMap<>();
		    
			for (Map.Entry<String, Map<String, JobProfile>> providers : jobIDs.getValue().entrySet()) {
				Map<String, JobProfile> mapTypeVMs = new HashMap<>();
		    	for (Map.Entry<String, JobProfile> typeVMs : providers.getValue().entrySet()) {
		    		if(mapTypeVMs.containsKey(Main.PRIVATE_PROVIDER_NAME)){
			    		System.out.println("Multiple typeVM ("+typeVMs.getKey()+") of different providers. "+providers.getKey()+" "+typeVMs.getKey()+" rejected. Rename typeVM!");//TODO posso appendere stringa del provider
			    	}else{
			    		mapTypeVMs.put(typeVMs.getKey(), typeVMs.getValue());
			    	}
		    	}
		    	mapProviders.put(Main.PRIVATE_PROVIDER_NAME, mapTypeVMs);
		    }
			mapJobIds.put(jobIDs.getKey(), mapProviders);
		}
		output.setMapJobProfiles(new JobProfilesMap(mapJobIds));
	}

	public static InstanceDataMultiProvider convertJSONs(Map<String,InstanceData_old> inputMap, InstanceDataMultiProvider output, boolean convertToPrivate) {
		for(Map.Entry<String, InstanceData_old> input : inputMap.entrySet()){
			addInstanceDataToOutputJSON(input.getValue(), output, convertToPrivate);
		}
		return output;
	}

	private static ClassParametersMap retrieveMapClassParameters(Map<String,ClassParameters> mapClassParameters,List<JobClass_old> list) {
		for (JobClass_old jobClass : list) {
			ClassParameters cp = new ClassParameters();
			cp.setD(jobClass.getD());
			cp.setHlow(1);//cp.setHlow(jobClass.getHlow());   //TODO CARE
			cp.setHup(1);//cp.setHup(jobClass.getHup());  	  //TODO CARE
			cp.setPenalty(jobClass.getJob_penalty());
			cp.setThink(jobClass.getThink());
			cp.setM(6);//cp.setM(jobClass.getM());  //CINECA 5x  6 TODO
			cp.setV(0); //cp.setV(jobClass.getV());  //CINECA 5x 0 TODO
			if(mapClassParameters.containsKey(jobClass.getId())){
					System.out.println("Multiple ClassParameters with ID: "+jobClass.getId());
			}else{
				mapClassParameters.put(jobClass.getId(), cp);
			}
		}
		return new ClassParametersMap(mapClassParameters);
	}
	
	private static JobProfilesMap retrieveMapJobProfiles(Map<String, Map<String, Map<String, JobProfile>>> mapJobProfiles, Map<TypeVMJobClassKey, Profile_old> mapProfiles, String provider) {
		if(mapJobProfiles==null){
			mapJobProfiles = new HashMap<>();
		}
		for (Map.Entry<TypeVMJobClassKey, Profile_old> entry : mapProfiles.entrySet()) {
			JobProfile p = new JobProfile();

			p.put("nm",entry.getValue().getNm());
			p.put("nr",entry.getValue().getNr());
			p.put("nm",entry.getValue().getCm());
			p.put("cr",entry.getValue().getCr());
			p.put("mavg",entry.getValue().getMavg());
			p.put("mmax",entry.getValue().getMmax());
			p.put("ravg",entry.getValue().getRavg());
			p.put("rmax",entry.getValue().getRmax());
			p.put("sh1max",entry.getValue().getSh1max());
			p.put("shtypavg",entry.getValue().getShtypavg());
			p.put("shtypmax",entry.getValue().getShtypmax());
			p.put("datasize", Main.DATASIZE);

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
			Map<String, Map<String, Map<String, PublicCloudParameters>>> mapPublicCloudParameters, Optional<Map<String, List<TypeVM>>> optional, String provider) {
		Map<String, List<TypeVM>> mapTypeVMs = optional.get();

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

	/*
	 * DEFAULT Private CLOUD Params
	 */
	private static void initializeMissingPrivateParameters(InstanceDataMultiProvider input) {
		input.setPrivateCloudParameters(getDefaultPrivateCloudParameters());
		List<String> l = new ArrayList<String>();
		l.addAll(input.getMapJobProfiles().getTypeVMs());
		input.setMapVMConfigurations(getDefaultVMConfiguration(l));
	}

	private static PrivateCloudParameters getDefaultPrivateCloudParameters() {
		PrivateCloudParameters p = new PrivateCloudParameters();
		p.setE(0.963585);    //CINECA 5xlarge rho_bar*1.15: 0.8379*1.15 = 0.963585 TODO
		p.setM(120); //CINECA 5xlarge 120GB TODO
		p.setN(4);  //CINECA 5xlarge [4,6] TODO
		p.setV(20); //CINECA 5xlarge 20vCPU TODO
		return p;
	}

	private static VMConfigurationsMap getDefaultVMConfiguration(List<String> lst) {
		Map<String, VMConfiguration> map = new HashMap<>();
		for (String s : lst) {
			VMConfiguration c = new VMConfiguration();
			c.setCore(20);	//CINECA 5xlarge 20vCPU
			c.setMemory(120); //CINECA 5xlarge 120GB
			c.setProvider("inHouse");
			c.setCost(Optional.of(0.963585)); //<- FIX CINECA 5xlarge rho_bar*1.15: 0.8379*1.15 = 0.963585
			map.put(s, c);
		}
		return new VMConfigurationsMap(map);
	}
}