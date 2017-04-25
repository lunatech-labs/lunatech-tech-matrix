angular.module('techmatrix').controller('SkillController',[
    '$scope',
    'RestService',
    'techType',
    'level',
    '$routeParams',
    'RestErrorService',
    '$mdDialog',
    '$cookies',
    '$mdToast',
    function($scope,RestService,techType,level,$routeParams,RestErrorService,$mdDialog,$cookies,$mdToast){

    $scope.data = {};
    $scope.data.skills = [];
    $scope.data.techType = techType;
    $scope.data.level = level;

    $scope.data.groupedSkills = {};

    $scope.data.loading = false;

    $scope.data.error = undefined;

    $scope.data.user = undefined;

    function onInit(){
        if($routeParams.error === 'nouser'){
            $scope.data.error = "You aren't logged with any user. Please user the Users page to log in."
        }
        $scope.data.user = $cookies.getObject('user');
        $scope.data.loading = true;
        RestService.getSkillMatrix().then(function(response){
            $scope.data.loading = false;
            $scope.data.skills = response.data.map(addSearchFilter);
            $scope.data.skills = $scope.data.skills.map(function(skill){
              var users = {}
              var isMissing = true;
              angular.forEach($scope.data.level, function(l){
                users[l.value] = skill.users.filter(function(u){
                  if(u.userName === $scope.data.user.full_name){
                    isMissing = false;
                  }
                  return u.level === l.value;
                });
              });
              skill.users = users;
              skill.isMissing = isMissing;
              return skill;
            });
            angular.forEach($scope.data.techType, function(techType){
              $scope.data.groupedSkills[techType.value] = $scope.data.skills.filter(function(s){
                return s.techType === techType.value;
              })
            });
        },function(response){
            RestErrorService.errorHandler(response)
            $scope.data.loading = false;
            console.log(response)
        });
    }

    function addSearchFilter(skill){
        skill.searchFilter = skill.techName + "," + skill.techType;
        return skill;
    }

    $scope.addSkillToUser = function(skill){
      $mdDialog.show({
        controller: function($scope, $mdDialog, level){
          $scope.levels = level;
          $scope.close = function(){
            $mdDialog.cancel();
          };
          $scope.select = function(l){
            $mdDialog.hide(l);
          };
          $scope.getLevelButtonClass = function(l){
            switch(l){
              case level.LONG_TIME_AGO.value:
                return 'long-time-bg-color';
              case level.NOVICE.value:
                return 'novice-bg-color';
              case level.INTERMEDIATE.value:
                return 'intermediate-bg-color';
              case level.EXPERT.value:
                return 'expert-bg-color';
              default:
                return '';
            }
          };
        },
        templateUrl: 'assets/javascripts/skill/add.skill.dialog.html',
        parent: angular.element(document.body),
        clickOutsideToClose:true
      }).then(function(levelSelected){
        addSkill({
          userId:$scope.data.user.id,
          body:{
            tech:{
              name:skill.techName,
              techType:skill.techType
            },
            skillLevel:levelSelected.value
          }
        })
      })
    };

    function addSkill(data){
      RestService.addSKill(data).then(function(response){
        $mdToast.show(
          $mdToast.simple()
            .textContent('Tech added')
            .hideDelay(3000)
        );
      },function(response){
          RestErrorService.errorHandler(response)
          $mdToast.show(
            $mdToast.simple()
              .textContent('Error adding tech')
              .hideDelay(3000)
          );
      });
    }

    $scope.showUsers = function(users){
      $mdDialog.show({
        controller: function(users, $scope, $mdDialog){
          $scope.users = users;
          $scope.close = function(){
            $mdDialog.cancel();
          };
        },
        templateUrl: 'assets/javascripts/skill/user.list.dialog.html',
        locals: {
          users: users
        },
        parent: angular.element(document.body),
        clickOutsideToClose:true
      })
    }

    $scope.getLevelClass = function(l){
      switch(l){
        case level.LONG_TIME_AGO.value:
          return 'long-time-color';
        case level.NOVICE.value:
          return 'novice-color';
        case level.INTERMEDIATE.value:
          return 'intermediate-color';
        case level.EXPERT.value:
          return 'expert-color';
        default:
          return '';
      }
    };

    $scope.getLevelBadgeClass = function(l){
      switch(l){
        case level.LONG_TIME_AGO.value:
          return 'long-time-bg-color';
        case level.NOVICE.value:
          return 'novice-bg-color';
        case level.INTERMEDIATE.value:
          return 'intermediate-bg-color';
        case level.EXPERT.value:
          return 'expert-bg-color';
        default:
          return '';
      }
    };

    onInit();
}]);