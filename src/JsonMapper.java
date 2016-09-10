
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
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobMLProfilesMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobProfile;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobProfilesMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PrivateCloudParameters;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PublicCloudParameters;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.PublicCloudParametersMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.VMConfigurationsMap;

public class JsonMapper {

	public static InstanceDataMultiProvider ConvertJson(InstanceData input) {
		InstanceDataMultiProvider output = new InstanceDataMultiProvider();

		output.setId(input.getId());

		if (input.getLstClass() != null && !input.getLstClass().isEmpty()) {
			output.setMapClassParameters(retrieveMapClassParameters(input.getLstClass()));
		}
		if (input.getMapJobMLProfiles() != null && input.getMapJobMLProfiles().getMapJobMLProfile() != null) {
			if (input.getMapJobMLProfiles().validate()) {
				output.setMapJobMLProfiles(retrieveMapJobMLProfiles(input.getMapJobMLProfiles()));
			} else {
				System.out.println("MLProfile not valid!");
			}
		} else {
			System.out.println("- JobMlProfilesNotFound");
		}
		if (input.getMapProfiles() != null && !input.getMapProfiles().isEmpty()) {
			output.setMapJobProfiles(retrieveMapJobProfiles(input.getMapProfiles(), input.getProvider()));
		}
		if (input.getMapTypeVMs() != null && input.getMapTypeVMs().isPresent()
				&& !input.getMapTypeVMs().get().isEmpty()) {
			output.setMapPublicCloudParameters(
					retrieveMapPublicCloudParameters(input.getMapTypeVMs(), input.getProvider()));
		}
		if (input.getMapVMConfigurations() != null && input.getMapVMConfigurations().isPresent()
				&& !input.getMapVMConfigurations().get().getMapVMConfigurations().isEmpty()
				&& input.getMapVMConfigurations().get().getMapVMConfigurations() != null
				&& input.getMapVMConfigurations().get().validate()) {
			output.setMapVMConfigurations(retrieveMapVMConfigurations(input.getMapVMConfigurations()));
		}
		if (input.getPrivateCloudParameters() != null && input.getPrivateCloudParameters().isPresent()
				&& input.getPrivateCloudParameters().get().validate()) {
			output.setPrivateCloudParameters(retrievePrivateCloudParameters(input.getPrivateCloudParameters()));
		}

		return output;
	}

	private static ClassParametersMap retrieveMapClassParameters(List<JobClass> lstClass) {
		Map<String, ClassParameters> mapClassParameters = new HashMap<>();
		for (JobClass jobClass : lstClass) {
			ClassParameters cp = new ClassParameters();
			cp.setD(jobClass.getD());
			cp.setHlow(jobClass.getHlow());
			cp.setHup(jobClass.getHup());
			cp.setPenalty(jobClass.getJob_penalty());
			cp.setThink(jobClass.getThink());
			cp.setM(jobClass.getM());
			cp.setV(jobClass.getV());
			mapClassParameters.put(jobClass.getId(), cp);
		}
		return new ClassParametersMap(mapClassParameters);
	}

	private static JobProfilesMap retrieveMapJobProfiles(Map<TypeVMJobClassKey, Profile> mapProfiles, String provider) {
		Map<String, Map<String, Map<String, JobProfile>>> mapJobProfiles = new HashMap<>();

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
					mapJobProfiles.get(entry.getKey().getJob()).get(provider).put(entry.getKey().getTypeVM(), p);
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
			Optional<Map<String, List<TypeVM>>> mapTypeVMsO, String provider) {
		Map<String, List<TypeVM>> mapTypeVMs = mapTypeVMsO.get();

		Map<String, Map<String, Map<String, PublicCloudParameters>>> mapPublicCloudParameters = new HashMap<>();

		for (Map.Entry<String, List<TypeVM>> mapEntry : mapTypeVMs.entrySet()) {
			for (TypeVM lstEntry : mapEntry.getValue()) {

				PublicCloudParameters p = new PublicCloudParameters();
				p.setEta(lstEntry.getEta());
				p.setR(lstEntry.getR());

				if (mapPublicCloudParameters.containsKey(mapEntry.getKey())) {
					if (mapPublicCloudParameters.get(mapEntry.getKey()).containsKey(provider)) {
						mapPublicCloudParameters.get(mapEntry.getKey()).get(provider).put(lstEntry.getId(), p);
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

	private static VMConfigurationsMap retrieveMapVMConfigurations(Optional<VMConfigurationsMap> mapVMConfigurationsO) {
		return mapVMConfigurationsO.get();
	}

	private static PrivateCloudParameters retrievePrivateCloudParameters(
			Optional<PrivateCloudParameters> privateCloudParameters) {
		return privateCloudParameters.get();
	}

	private static JobMLProfilesMap retrieveMapJobMLProfiles(JobMLProfilesMap mapJobMLProfiles) {
		return mapJobMLProfiles;
	}

}
