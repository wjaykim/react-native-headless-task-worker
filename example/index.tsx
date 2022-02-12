import { AppRegistry } from "react-native";
import App from "./src/App";
import { name as appName } from "./app.json";

const MyTask = async (taskData: { message: string }) => {
  console.log("MyTask", taskData.message);
};

AppRegistry.registerHeadlessTask("MyTask", () => MyTask);

AppRegistry.registerComponent(appName, () => App);
