/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine;


import org.activiti.engine.impl.cmd.ProcessNodeJump;

/**
 * Interface implemented by all classes that expose the Activiti services.
 * 
 * @author Joram Barrez 
 */
public interface EngineServices {

  RepositoryService getRepositoryService();
  
  RuntimeService getRuntimeService();
  
  FormService getFormService();
  
  TaskService getTaskService();
  
  HistoryService getHistoryService();
  
  IdentityService getIdentityService();
  
  ManagementService getManagementService();
  
  DynamicBpmnService getDynamicBpmnService();
  
  ProcessEngineConfiguration getProcessEngineConfiguration();
  ProcessNodeJump getProcessNodeJum();
}
