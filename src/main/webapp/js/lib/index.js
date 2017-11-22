'use strict';


angular.module('app', []).controller('MainCtrl', function ($scope, $document) {
    $scope.navLabel = "Nuova stagione";
    $scope.humans = 2;
    $scope.actualMatches = {};
    $scope.actualRound;
    $scope.campionatotable = [];
    $scope.round;

    $scope.showNext = function () {
        refresh();
    };
    $scope.showPrev = function () {
        $scope.actualRound--;
        refresh(true);
    };

    function actual() {
        get('actual', function (r) {
            if (r.simple && r.simple.idround > 0) {
                $scope.situation = 'main';
                $scope.actualRound = r.simple.idround;
                $scope.round = r.simple;
                angular.element(window.document).find('.tab-' + r.simple.league).click();
            } else {
                $scope.situation = 'start';
            }
        });
    }
    
    refresh();
    angular.element(window.document).find('.maincontainer').fadeIn(300);
    

    function refresh(skipactual) {
        if (!skipactual) {
            actual();
        }
        console.log($scope.actualRound)
        if ($scope.situation === 'start') {
            $scope.navLabel = "Nuova stagione";
            $scope.navLabelClick = function () {
                $scope.situation = 'teamchooser';
                refresh(true);
            };
        } else if ($scope.situation === 'teamchooser') {
            get('teamchooser', function (r) {
                $scope.teamchooserTeams = r.simple;
                $scope.navLabel = "Comincia stagione";
                $scope.navLabelClick = function () {
                    post('postteamchooser', postRequestContent(readSelectedTeams($scope)), function (r) {
                        if (!r.ok) {
                            $scope.teamchooserError = r.error;
                        } else {
                            $scope.teamchooserError = 'ok';
                            $scope.situation = 'main';
                            $scope.actualRound = r.simple;
                            refresh();
                        }
                    });
                };
            });
        } else if ($scope.situation === 'main') {
            get('round?idround=' + $scope.actualRound, function (r) {
                if (!r.ok) {
                    refresh();
                    return;
                }
                
                $scope.actualMatches = {
                    played: r.played,
                    matches: r.simple
                };
                $scope.campionatotable = r.table.rows;

                if (!r.played) {
                    $scope.navLabel = "Salva risultati";
                    $scope.navLabelClick = function () {
                        post('postround',
                                postRequestContent({matches: $scope.actualMatches.matches, idround: $scope.actualRound}),
                                function (r) {
                                    refresh(true);
                                });
                    };
                } else {
                    $scope.navLabel = "Prossimo turno";
                    $scope.navLabelClick = function () {
                        refresh();
                    };

                }
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
            async: false,
            success: callback,
            headers: headers, error: function (result) {
                console.error('error');
            }});
    }

});




