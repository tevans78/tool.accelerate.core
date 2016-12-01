/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

 //service for communicating with the app accelerator

angular.module('appAccelerator')
   .factory('appacc',
    [      '$log','$q','$http',
  function ($log,  $q,  $http) {
     $log.debug("Initialising AppAccelerator service");

     var serviceURL = "/start/api/v1";
     var techURL = serviceURL + "/tech";  //where to get the technology types from
     var dataURL = serviceURL + "/data?";              //tech=rest&deploy=local&name=libertyProject&workspace=642f3151-c9b6-4d5c-b185-4c29b8
     var optionsURL = "/start/options";
     var bluemix = false;
     //put the selected technologies here so that it can seen by multiple controllers
     var selectedTechnologies = [];  //list of technologies currrently selected by the user
     var projectName = undefined;

     var getTechnologies = function() {
        $log.debug("AppAccelerator : GET : available technology list");

        var q = $q.defer();
        $http({
          url: techURL,
          method: 'GET'
          }).then(function(response) {
            // 200: technologies discovered
            $log.debug(response.status + ' ' + response.statusText + " %o - OK", response.data);
            var data = [];
            if ( response.status === 200 ) {
              data = angular.fromJson(response.data);
            }
            $log.debug("getTechnologies returning %o", data);
            q.resolve(data);
          }, function(response) {
            $log.debug(response.status + ' ' + response.statusText + " %o - FAILED", response.data);
            // TODO: Alert -- problem occurred
            // return empty set for decent experience, or could insert some default values here ...
            q.reject([]);
          });

          return q.promise;
      };

      //get the technology options for a given technology
      var getTechOptions = function(technology) {
        $log.debug("AppAccelerator : GET : technology options");
        var q = $q.defer();
        if(!technology.options) {
          //there are no options so resolve and return immediately
          q.resolve(undefined);
        } else {
          $http({
            url: optionsURL + '/' + technology.id + '/' + technology.id + '.html',
            method: 'GET'
            }).then(function(response) {
              // 200: the options template was found
              $log.debug(response.status + ' ' + response.statusText + " %o - OK");
              q.resolve(response.data);
            }, function(response) {
              $log.debug(response.status + ' ' + response.statusText + " %o - FAILED", response.data);
              // TODO: Alert -- problem occurred
              // return empty set for decent experience, or could insert some default values here ...
              q.reject(undefined);
            });
        }
        return q.promise;
      };
      
      var createDownloadUrl = function() {
          var url = undefined;
          var selected = "";   //the list of selected technologies
          for(var i = 0; i < selectedTechnologies.length; i++) {
            var tech = selectedTechnologies[i];
            if(tech.selected) {
              if(selected != "") selected += "&";
              selected += ("tech=" + tech.id);
              $log.debug("selected has value:" + selected);
            }
            $log.debug("Technology %o is not selected so not adding.", tech);
          }
          if(selected != "") {
            //something has been selected, so proceed
            var deployType = bluemix ? "&deploy=bluemix" : "&deploy=local";
            url = dataURL + selected + deployType + "&name=" + projectName;
            $log.debug("Constructed " + url);
            
          } else {
            //something has gone wrong, nothing has been selected
            $log.debug("Nothing has been selected.");
          }
          return url;
      }

      //how many technologies have currently been selected
      var getSelectedCount = function() {
        return selectedTechnologies.length;
      }

      var addTechnology = function(technology) {
        $log.debug("Adding technology:" + technology.id);
        technology.selected = true;
        $log.debug("technology.selected for technology " + technology.id + " has value:" + technology.selected);
        for(var i = 0; i < selectedTechnologies.length; i++) {
          if(selectedTechnologies[i].id == technology.id) {
            //already added, so ignore and return
          }
        }
        selectedTechnologies.push(technology);
      }

      var removeTechnology = function(technology) {
        technology.selected = false;
        for(var i = 0; i < selectedTechnologies.length; i++) {
          if(selectedTechnologies[i].id == technology.id) {
            //id's match so remove from the list
            selectedTechnologies.splice(i, 1);
            return;
          }
        }
      }

      //true if a technology has been selected, false if not
      var isSelected = function(id) {
        for(var i = 0; i < selectedTechnologies.length; i++) {
          if(selectedTechnologies[i].id == id) {
            return true;
          }
        }
        return false;
      }
      
      var deployToBluemix = function(bool) {
        if (bool != undefined) {
          bluemix = bool;
        }
        return bluemix;
      }
      
      var updateName = function(name) {
        if (name != undefined) {
          projectName = name;
        }
        return projectName;
      }

      return {
        getTechnologies: getTechnologies,
        createDownloadUrl : createDownloadUrl,
        getTechOptions : getTechOptions,
        getSelectedCount : getSelectedCount,
        addTechnology : addTechnology,
        removeTechnology : removeTechnology,
        isSelected : isSelected,
        deployToBluemix : deployToBluemix,
        updateName : updateName
      };
  }]);
