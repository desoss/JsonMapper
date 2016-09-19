package NewToOld;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.InstanceData_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.InstanceDataMultiProvider;
import it.polimi.diceH2020.SPACE4Cloud.shared.settings.Scenarios;

public class ScenarioValidation {
	
	
	public String scenarioValidation(InstanceDataMultiProvider instanceDataMultiProvider, Scenarios scenario){
		String returnString = new String();
		if(instanceDataMultiProvider.getMapJobProfiles()==null || instanceDataMultiProvider.getMapClassParameters()==null){
			returnString = "Json is missing some required parameters(MapJobProfiles or MapClassParameters)!";
			return returnString;
		}

		switch (scenario) {
			case PrivateAdmissionControl:
				if(instanceDataMultiProvider.getPrivateCloudParameters()==null||instanceDataMultiProvider.getMapVMConfigurations()==null){
					returnString = "Json is missing some required parameters(PrivateCloudParameters or MapVMConfigurations)!";
					return returnString;
				}
				if (!instanceDataMultiProvider.getPrivateCloudParameters().validate()) {
					returnString = "Private Cloud Parameters uploaded in Json aren't valid!";
					return returnString;
				}
				if (!instanceDataMultiProvider.getMapVMConfigurations().validate()) {
					returnString = "VM Configurations uploaded in Json aren't valid!";
					return returnString;
				}
				if(instanceDataMultiProvider.getProvidersList().size() != 1){
					returnString = "A private scenario cannot have multiple providers!(call you providers:\"inHouse\")";
					return returnString;
				}
				break;

			case PrivateNoAdmissionControl:
				if(instanceDataMultiProvider.getMapVMConfigurations()==null){
					returnString = "Json is missing some required parameters(MapVMConfigurations)!";
					return returnString;
				}
				if(instanceDataMultiProvider.getMapVMConfigurations().getMapVMConfigurations()==null){
					returnString = "Json is missing some required parameters(MapVMConfigurations)!";
					return returnString;
				}
				if (!instanceDataMultiProvider.getMapVMConfigurations().validate()) {
					returnString = "VM Configurations uploaded in Json aren't valid!";
					return returnString;
				}
				if(instanceDataMultiProvider.getProvidersList().size() != 1){
					returnString = "A private scenario cannot have multiple providers!(call you providers:\"inHouse\")";
					return returnString;
				}
				break;

			case PublicPeakWorkload:
				if(instanceDataMultiProvider.getMapPublicCloudParameters()==null){
					returnString = "Json is missing some required parameters(MapPublicCloudParameters)!";
					return returnString;
				}
				if (!instanceDataMultiProvider.getMapPublicCloudParameters().validate()) {
					returnString = "Public Cloud Parameters uploaded in Json aren't valid!";
					return returnString;
				}
				break;

			case PublicAvgWorkLoad:
				break;

			default:
				new Exception("Error with scenario files");
				break;
		}
		return "ok";
	}

	public String scenarioValidation(InstanceData_old instanceData, Scenarios scenario){
		String returnString = new String();
		if(instanceData.getLstClass()==null || instanceData.getMapProfiles() == null){
			returnString = "Json is missing some required parameters(MapJobProfiles or MapClassParameters)!";
			return returnString;
		}

		switch (scenario) {
			case PrivateAdmissionControl:
				if(!instanceData.getPrivateCloudParameters().isPresent()||!instanceData.getMapVMConfigurations().isPresent()){
					returnString = "Json is missing some required parameters(PrivateCloudParameters or MapVMConfigurations)!";
					return returnString;
				}
				if(instanceData.getPrivateCloudParameters().get()==null||instanceData.getMapVMConfigurations().get()==null){
					returnString = "Json is missing some required parameters(PrivateCloudParameters or MapVMConfigurations)!";
					return returnString;
				}
				if (!instanceData.getPrivateCloudParameters().get().validate()) {
					returnString = "Private Cloud Parameters uploaded in Json aren't valid!";
					return returnString;
				}
				if (!instanceData.getMapVMConfigurations().get().validate()) {
					returnString = "VM Configurations uploaded in Json aren't valid!";
					return returnString;
				}
				break;

			case PrivateNoAdmissionControl:
				if(!instanceData.getMapVMConfigurations().isPresent()){
					returnString = "Json is missing some required parameters(MapVMConfigurations)!";
					return returnString;
				}
				if(instanceData.getMapVMConfigurations().get()==null){
					returnString = "Json is missing some required parameters(MapVMConfigurations)!";
					return returnString;
				}
				if (!instanceData.getMapVMConfigurations().get().validate()) {
					returnString = "VM Configurations uploaded in Json aren't valid!";
					return returnString;
				}
				break;

			case PublicPeakWorkload:
				if(!instanceData.getMapTypeVMs().isPresent()){
					returnString = "Json is missing some required parameters(MapPublicCloudParameters)!";
					return returnString;
				}
				if(instanceData.getMapTypeVMs().get()==null){
					returnString = "Json is missing some required parameters(MapPublicCloudParameters)!";
					return returnString;
				}
				//TODO validation 
				break;

			case PublicAvgWorkLoad:
				break;

			default:
				new Exception("Error with scenario files");
				break;
		}
		return "ok";
	}
}
