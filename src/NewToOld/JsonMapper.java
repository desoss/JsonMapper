/*
Copyright 2016 Jacopo Rigoli

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package NewToOld;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.InstanceData_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.JobClass_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.Profile_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.*;
import it.polimi.diceH2020.SPACE4Cloud.shared.settings.Scenarios;

import java.util.*;
import java.util.Map.Entry;

/**
 * from InstanceData_old to InstanceDataMultiProvider 
 * COMPLETE WITH REFLECTION BEFORE INTEGRETING
 */
public class JsonMapper {

	public static List<InstanceData_old> getInstanceDataList(InstanceDataMultiProvider instanceDataMultiProvider, Scenarios scenario){
		List<InstanceData_old> instanceDataList = new ArrayList<>();

		switch(scenario){
			case PrivateAdmissionControl:
				instanceDataList = buildInstanceDataPrPeak(instanceDataMultiProvider);
				break;
			case PrivateAdmissionControlWithPhysicalAssignment:
				instanceDataList = buildInstanceDataPrPeakWithPhysicalAssignment(instanceDataMultiProvider);
				break;
			case PrivateNoAdmissionControl:
				instanceDataList = buildInstanceDataPrAvg(instanceDataMultiProvider);
				break;
			case PublicPeakWorkload:
				instanceDataList = buildInstanceDataPuPeak(instanceDataMultiProvider);
				break;
			case PublicAvgWorkLoad:
				instanceDataList = buildInstanceDataPuAvg(instanceDataMultiProvider);
				break;
			default:
				new Exception("Error with the selected scenario");
				break;
		}

		return instanceDataList;
	}

	private static List<InstanceData_old> buildInstanceDataPrAvg(InstanceDataMultiProvider instanceDataMultiProvider){
		List<InstanceData_old> instanceDataList = new ArrayList<InstanceData_old>();
		for(String provider : instanceDataMultiProvider.getProvidersList()){ //useless loop, used for compliance 
			InstanceData_old instanceData = buildPartialInput(instanceDataMultiProvider,provider);
			instanceData.setScenario(Optional.of(Scenarios.PrivateNoAdmissionControl));
			instanceData.setMapVMConfigurations(Optional.of(instanceDataMultiProvider.getMapVMConfigurations()));
			instanceDataList.add(instanceData);
		}
		return instanceDataList;
	}

	private static List<InstanceData_old> buildInstanceDataPrPeak(InstanceDataMultiProvider instanceDataMultiProvider){
		List<InstanceData_old> instanceDataList = new ArrayList<InstanceData_old>();

		for(String provider : instanceDataMultiProvider.getProvidersList()){ //useless loop, used for compliance 
			InstanceData_old instanceData = buildPartialInput(instanceDataMultiProvider,provider);
			instanceData.setScenario(Optional.of(Scenarios.PrivateAdmissionControl));
			instanceData.setMapVMConfigurations(Optional.of(instanceDataMultiProvider.getMapVMConfigurations()));
			instanceData.setPrivateCloudParameters(Optional.of(instanceDataMultiProvider.getPrivateCloudParameters()));
			instanceDataList.add(instanceData);
		}
		return instanceDataList;
	}

	private static List<InstanceData_old> buildInstanceDataPrPeakWithPhysicalAssignment(InstanceDataMultiProvider instanceDataMultiProvider){
		List<InstanceData_old> instanceDataList = new ArrayList<InstanceData_old>();

		for(String provider : instanceDataMultiProvider.getProvidersList()){ //useless loop, used for compliance 
			InstanceData_old instanceData = buildPartialInput(instanceDataMultiProvider,provider);
			instanceData.setScenario(Optional.of(Scenarios.PrivateAdmissionControlWithPhysicalAssignment));
			instanceData.setMapVMConfigurations(Optional.of(instanceDataMultiProvider.getMapVMConfigurations()));
			instanceData.setPrivateCloudParameters(Optional.of(instanceDataMultiProvider.getPrivateCloudParameters()));
			instanceDataList.add(instanceData);
		}
		return instanceDataList;
	}

	private static List<InstanceData_old> buildInstanceDataPuAvg(InstanceDataMultiProvider instanceDataMultiProvider){
		List<InstanceData_old> instanceDataList = new ArrayList<InstanceData_old>();
		for(String provider : instanceDataMultiProvider.getProvidersList()){
			InstanceData_old instanceData = buildPartialInput(instanceDataMultiProvider,provider);
			instanceData.setScenario(Optional.of(Scenarios.PublicAvgWorkLoad));
			instanceDataList.add(instanceData);
		}
		return instanceDataList;
	}

	private static List<InstanceData_old> buildInstanceDataPuPeak(InstanceDataMultiProvider instanceDataMultiProvider){
		List<InstanceData_old> instanceDataList = new ArrayList<InstanceData_old>();
		for(String provider : instanceDataMultiProvider.getProvidersList()){
			InstanceData_old instanceData = buildPartialInput(instanceDataMultiProvider,provider);
			instanceData.setScenario(Optional.of(Scenarios.PublicPeakWorkload));
			instanceData.setMapTypeVMs(Optional.of(fromMapPublicCloudParametersToMapTypeVMs(instanceDataMultiProvider.getMapPublicCloudParameters(), provider)));
			instanceDataList.add(instanceData);
		}

		return instanceDataList;
	}

	/**
	 * This method creates the initial instanceData. <br>
	 * It does 2 mandatory mappings from InstanceDataMultiProvider: <br>
	 * &emsp; •maps mapClassParameters to lstClass<br>
	 * &emsp; •maps mapJobProfile to mapProfiles 
	 * @param input
	 * @return InstanceData with an initial set of parameters, those ones that are mandatory
	 */
	private static InstanceData_old buildPartialInput(InstanceDataMultiProvider input, String provider){

		List<JobClass_old> lstClasses = fromMapClassParametersToLstClass(input.getMapClassParameters());
		Map<TypeVMJobClassKey, Profile_old> map = fromMapJobProfileToMapProfiles(input.getMapJobProfiles(), provider);

		InstanceData_old partialInput = new InstanceData_old();

		partialInput.setId(input.getId());
		partialInput.setGamma(1500);
		partialInput.setLstClass(lstClasses);
		partialInput.setMapProfiles(map);
		partialInput.setProvider(provider);
		partialInput.setMapJobMLProfiles(input.getMapJobMLProfiles());

		return partialInput;
	}

	private static List<JobClass_old> fromMapClassParametersToLstClass(ClassParametersMap input){
		List<JobClass_old> lstClasses = new ArrayList<>();

		for (Map.Entry<String, ClassParameters> entry : input.getMapClassParameters().entrySet()) {
			JobClass_old jc = new JobClass_old();

			jc.setD(entry.getValue().getD());
			jc.setHlow(entry.getValue().getHlow());
			jc.setHup(entry.getValue().getHup());
			jc.setId(entry.getKey());
			jc.setJob_penalty(entry.getValue().getPenalty());
			jc.setThink(entry.getValue().getThink());
			jc.setM(entry.getValue().getM());
			jc.setV(entry.getValue().getV());

			lstClasses.add(jc);
		}

		return lstClasses;
	}

	private static Map<TypeVMJobClassKey, Profile_old> fromMapJobProfileToMapProfiles(JobProfilesMap input, String provider){
		Map<TypeVMJobClassKey, Profile_old> map = new HashMap<TypeVMJobClassKey, Profile_old>();

		for (Entry<String, Map<String, JobProfile>> jobIDs : input.get_IdVMTypes_Map(provider).entrySet()) {
			for (Map.Entry<String, JobProfile> typeVMs : jobIDs.getValue().entrySet()) {
				TypeVMJobClassKey key = new TypeVMJobClassKey();
				Profile_old p = new Profile_old();

				key.setJob(jobIDs.getKey());
				key.setTypeVM(typeVMs.getKey());
				
//					//TODO introspection reflection COMPLETE BEFORE INTEGRATING
//				for(Map.Entry<String, Double> profileFeature : typeVMs.getValue().getProfileMap().entrySet()){
//					p.put(profileFeature.getKey(), profileFeature.getValue());
//				}

				map.put(key, p);
			}
		}
		return map;
	}

	private static Map<String,List<TypeVM>> fromMapPublicCloudParametersToMapTypeVMs(PublicCloudParametersMap input, String provider){
		Map<String,List<TypeVM>> map = new HashMap<String,List<TypeVM>>();
		for (Entry<String, Map<String, PublicCloudParameters>> jobIDs : input.get_IdVMTypes_Map(provider).entrySet()) {
			List<TypeVM> vmList = new ArrayList<TypeVM>();
			for (Entry<String, PublicCloudParameters> typeVMs : jobIDs.getValue().entrySet()) {

				TypeVM vm = new TypeVM();
				vm.setEta(typeVMs.getValue().getEta());
				vm.setId(typeVMs.getKey()); //id of vmType
				vm.setR(typeVMs.getValue().getR());
				vmList.add(vm);

			}
			map.put(jobIDs.getKey(),vmList);
		}
		return map;
	}

}
