'use strict';


angular.module('app', []).controller('MainCtrl', function ($scope, $document) {
    $scope.navLabel = "Nuova stagione";
    $scope.humans = 1;
    $scope.actualMatches = {};
    $scope.actualRound;
    $scope.campionatotable = [];
    $scope.round;
    $scope.buttonDisabled = false;

    $scope.showNext = function () {
        refresh();
    };
    $scope.showPrev = function () {
        $scope.actualRound--;
        refresh(true);
    };
    $scope.chooseDatabase = function (db) {
        $scope.selecteddb = db;
        postDB();
    };

    function actual() {
        get('actual', function (r) {
            if (r.simple && r.simple.idround > 0) {
                $scope.situation = 'main';
                $scope.actualRound = r.simple.idround;
                $scope.round = r.simple;
                console.log(r)
                angular.element(window.document).find('.tab-' + r.simple.league.replace(" ", "-")).click();
            } else {
                console.log('actual')
                $scope.situation = 'start';
                $scope.databases = r.simple || $scope.databases;
            }
        });
    }

    refresh();
    angular.element(window.document).find('.maincontainer').fadeIn(300);

    function postDB() {
        post('postdbchooser', postRequestContent({selected: $scope.selecteddb || "0"}), function (r) {
            console.log(r.simple)
            console.log('qui' , $scope.situation)
            $scope.situation = r.simple ? 'main' : 'teamchooser';
            console.log('qui' , $scope.situation)
            refresh(!r.simple);
        });
    }


    function refresh(skipactual) {
        if (!skipactual) {
            actual();
        }
        console.log('REF', $scope)
        if ($scope.situation === 'start') {
            $scope.navLabel = "Nuovo gioco";
            $scope.navLabelClick = function () {
                postDB();
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
                $scope.round = r.round;

                $scope.actualMatches = {
                    played: r.played,
                    matches: r.simple
                };
                $scope.campionatotable = r.table.rows;
                $scope.billboardCl = r.billboardCh;
                $scope.billboardEl = r.billboardEl;
                $scope.billboardCo = r.billboardCo;

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

    $scope.getBillboardGoal = function (cal, r, index, home) {
        return cal && cal.playedRounds > r ? (home ? cal.matches[r][index].goalHome : cal.matches[r][index].goalAway) : '-';
    };
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
            success: function (r) {
                callback(r);
                $scope.buttonDisabled = false;
            },
            headers: headers, error: function (result) {
                console.error('error');
            }});
    }

});




