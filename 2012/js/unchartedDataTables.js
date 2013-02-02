/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/20/11
 * Time: 11:54 PM
 * To change this template use File | Settings | File Templates.
 */

function createResultsTable(results) {
  return results.dataTable( {
    "bProcessing": false,
    "bServerSide" : true,
    "bSort" : true,
    "bStateSave" : false,
    "bLengthChange" : false,
    "bFilter" : false,
    "bJQueryUI": true,
    "bInfo": false,
    "sScrollY": "200px",
    "iDisplayLength": 30,
    "aoColumns": [
      /* ID */  { "asSorting": [ "desc" ], "bVisible": false },
      /* Round */   { "sWidth" : "30px", "bSortable": false, "sClass": "right" },
      /* Pick */  { "sWidth" : "30px", "bSortable": false, "sClass": "right" },
      /* DisplayUser */ { "sWidth" : "70px", "bSortable": false },
      /* DraftPos */ { "sWidth" : "30px", "bSortable": false },
      /* PlayerName */  { "sWidth" : "100px", "bSortable": false,
            "fnRender": function ( oObj ) {
                var aData = oObj.aData[5];
                var keeper = oObj.aData[6];
                if (keeper == 1) {
                    return "<i>" + aData + "</i>";
                } else {
                    return aData;
                }
            } },
      /* Keeper */  { "bVisible": false }
    ],
    "sAjaxSource": "json_draftresults.php"
  } );

}

function createMyTeamTable(myTeam, commish) {
  if (commish) {
      ajaxUrl = "json_teamLineup.php?commish=1";
  } else {
      ajaxUrl = "json_teamLineup.php";
  }
  return myTeam.dataTable( {
    "bProcessing": false,
    "bServerSide" : true,
    "bSort" : false,
    "bStateSave" : false,
    "bLengthChange" : false,
    "bFilter" : false,
    "bJQueryUI": true,
    "bInfo": false,
    "bPaginate": false,
      "aoColumns": [
          /* DraftPos */ { "sWidth" : "30px", "bSortable": false },
          /* PlayerName */  { "sWidth" : "100px", "bSortable": false },
          /* IsOpen */  { "bVisible": false }
      ],
    "sAjaxSource": ajaxUrl
  } );

}

function createPlayerCatTable(playerCats, callback, commish) {
    if (commish) {
        ajaxUrl = "json_unclaimedPlayerCats.php?commish=1";
    } else {
        ajaxUrl = "json_unclaimedPlayerCats.php";
    }
  return playerCats.dataTable( {
    "bProcessing": false,
    "bServerSide" : true,
    "bSort" : true,
    "bStateSave" : false,
    "bLengthChange" : false,
    "sScrollY": "800px",
    "iDisplayLength": 50,
    "aaSorting": [[19,'desc']],
    "bJQueryUI": true,
    "bAutoWidth": false,
//    "bDeferRender": true,
    "sPaginationType": "full_numbers",
    "bInfo" : false,
    "fnServerData" : callback,
    "sDom" : '<"#RG">t<"F"p>',
    "aoColumns": [
      /* LastName */   { "bSortable": true, "sTitle": "Last" },
      /* FirstName */   { "bSortable": true, "sTitle": "First" },
      /* MLB */   { "bSortable": true },
      /* Position */  { "bVisible" : true, "bSortable" : true },
      /* Eligibility */  { "bSortable": true },
      /* OBP */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
            var aData = oObj.aData[5];
            if (aData == "") {
                return "";
            } else {
                return truncateBAstyle(aData);
            }
        } },
      /* SLG */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
            var aData = oObj.aData[6];
            if (aData == "") {
                return "";
            } else {
                return truncateBAstyle(aData);
            }
        } },
      /* RHR */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* RBI */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* HR */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* SBC */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* INN */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right",
            "fnRender": function ( oObj ) {
                var aData = oObj.aData[11];
                if (aData == "") {
                    return "";
                } else {
                    return parseFloat(aData).toFixed(0);
                }
            } },
      /* ERA */  { "asSorting": ["asc", "desc"], "bSortable": true, "sClass": "right",
            "fnRender": function ( oObj ) {
                var aData = oObj.aData[12];
                if (aData == "") {
                    return "";
                } else {
                    return parseFloat(aData).toFixed(2);
                }
            } },
      /* WHIP */  { "asSorting": ["asc", "desc"], "bSortable": true, "sClass": "right",
            "fnRender": function ( oObj ) {
                var aData = oObj.aData[13];
                if (aData == "") {
                    return "";
                } else {
                    return parseFloat(aData).toFixed(3);
                }
            } },
      /* WL */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* K */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* S */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* Rank */  { "asSorting": ["asc"], "bSortable": true, "sClass": "right" },
      /* Role */  { "bVisible" : false },
      /* Rating */  { "asSorting": ["desc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return parseFloat(oObj.aData[19]).toFixed(2);
        } }
    ],
    "sAjaxSource": ajaxUrl
  } );
}


    function truncateBAstyle(statThing) {
      var asStr = parseFloat(statThing).toFixed(3) + '';
      return asStr.substring(1);
    }
    function truncateERAstyle(statThing) {
      return parseFloat(statThing).toFixed(2) + '';
    }
    function truncateWHIPstyle(statThing) {
      return parseFloat(statThing).toFixed(3) + '';
    }
