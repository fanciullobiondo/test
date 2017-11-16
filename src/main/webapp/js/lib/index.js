'use strict';


angular.module('app', []).controller('MainCtrl', function ($scope, $interval) {
    $scope.navLabel = "Nuova stagione";
    $scope.humans = 2;
    $scope.actualMatches = {};
    $scope.actualRound;
    $scope.campionatotable = [];
    get('actual', function (r) {
        console.log('chiedo actual' , r)
        if (r.simple > 0) {
            $scope.situation = 'main';
            $scope.actualRound = r.simple;
        } else {
            $scope.situation = 'start';
        }
        refresh();
    });

    function refresh() {
        if ($scope.situation === 'start') {
            $scope.navLabel = "Nuova stagione";
            $scope.navLabelClick = function () {
                $scope.situation = 'teamchooser';
                refresh();
            };
        } else if ($scope.situation === 'teamchooser') {
            get('teamchooser', function (r) {
                $scope.teamchooserTeams = r.simple;
                console.log('teams', r);
                $scope.navLabel = "Comincia stagione";
                $scope.navLabelClick = function () {
                    post('postteamchooser', postRequestContent(readSelectedTeams($scope)), function (r) {
                        if (!r.ok) {
                            $scope.teamchooserError = r.error;
                        } else {
                            $scope.teamchooserError =  'ok';
                            $scope.situation = 'main';
                            $scope.actualRound = r.simple;
                            
                            refresh();
                            
                        }
                    });
                };
            });
        } else if ($scope.situation === 'main') {
            get('round?idround=' + $scope.actualRound, function (r) {
                $scope.navLabel = "Salva risultati";
                console.log('qui')
                $scope.actualMatches = {
                    played: r.played,
                    matches: r.simple
                };
                console.log(r);
                $scope.campionatotable = r.table.rows;
                
                $scope.navLabelClick = function () {
                    console.log($scope.actualMatches);
                    post('postround', postRequestContent({matches: $scope.actualMatches.matches, idround: $scope.actualRound}), function (r) {
                        console.log(r);
                        refresh();
                    });
                };
            });

        }

    }
    $scope.getNumber = function (num) {
        return new Array(num);
    };
    function readSelectedTeams($scope) {
        var selected = [];
        $('.choose-team').each(function () {
            selected.push($(this).val());
        });
        return {
            selected: selected,
            count: $scope.humans
        }
    }
    function postRequestContent(e) {
        return JSON.stringify(e);
    }

    function get(target, callback) {
        request(target, 'GET', null, null, callback);
    }
    function post(target, data, callback) {
        request(target, 'POST', data, {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, callback);
    }
    function request(target, type, data, headers, callback) {
        $.ajax({url: window.location.origin + window.location.pathname + "rest/api/" + target,
            type: type,
            data: data,
            async:false,
            success: callback,
            headers: headers, error: function (result) {
                console.error('error');
            }});
    }

});




