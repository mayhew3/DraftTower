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
      /* Team */ { "sWidth" : "100px", "bSortable": false },
      /* Player */  { "sWidth" : "100px", "bSortable": false }
    ],
    "sAjaxSource": "json_draftresults.php"
  } );

}

function createUnclaimedTable(unclaimed) {
  return unclaimed.dataTable( {
    "bProcessing": false,
    "bServerSide" : true,
    "bSort" : true,
    "bStateSave" : false,
    "bLengthChange" : false,
    "bFilter" : false,
    "sScrollY": "500px",
    "iDisplayLength": 50,
    "aaSorting": [[3,'desc']],
    "bInfo": false,
    "bJQueryUI": true,
    "sPaginationType": "full_numbers",
    "aoColumns": [
      /* Player */   { "bSortable": true, "sWidth" : "150px" },
      /* Position */  { "bSortable": true },
      /* Rank */  { "asSorting": ["asc"], "bSortable": true, "sClass": "right" },
      /* Rating */  { "asSorting": ["desc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return parseFloat(oObj.aData[3]).toFixed(2);
        } }
    ],
    "sAjaxSource": "json_unclaimedPlayers.php"
  } );
}

function createBatterTable(batters, callback) {
  return batters.dataTable( {
    "bProcessing": false,
    "bServerSide" : true,
    "bSort" : true,
    "bStateSave" : false,
    "bLengthChange" : false,
    "sScrollY": "300px",
    "iDisplayLength": 50,
    "aaSorting": [[10,'desc']],
    "bJQueryUI": true,
    "sPaginationType": "full_numbers",
    "bInfo" : false,
    "fnServerData" : callback,
    "sDom" : '<"#RG">t<"F"p>',
    "aoColumns": [
      /* Player */   { "sWidth" : "200px", "bSortable": true },
      /* Position */  { "bVisible" : true, "bSortable" : true },
      /* Eligibility */  { "bSortable": true },
      /* OBP */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return truncateBAstyle(oObj.aData[3]);
        } },
      /* SLG */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return truncateBAstyle(oObj.aData[4]);
        } },
      /* RHR */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* RBI */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* HR */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* SBC */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* Rank */  { "asSorting": ["asc"], "bSortable": true, "sClass": "right" },
      /* Rating */  { "asSorting": ["desc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return parseFloat(oObj.aData[10]).toFixed(2);
        } }
    ],
    "sAjaxSource": "json_unclaimedBatters.php"
  } );
}


    function truncateBAstyle(statThing) {
      var asStr = parseFloat(statThing).toFixed(3) + '';
      return asStr.substring(1);
    }

function createPitcherTable(pitchers) {
  return pitchers.dataTable( {
    "bProcessing": false,
    "bServerSide" : true,
    "bSort" : true,
    "bStateSave" : false,
    "bLengthChange" : false,
    "sScrollY": "300px",
    "iDisplayLength": 50,
    "aaSorting": [[9,'desc']],
    "bJQueryUI": true,
    "sPaginationType": "full_numbers",
    "bInfo" : false,
    "sDom" : '<"#RP">t<"F"p>',
    "aoColumns": [
      /* Player */   { "sWidth" : "200px", "bSortable": true },
      /* INN */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return parseFloat(oObj.aData[1]).toFixed(1);
        } },
      /* ER */  { "asSorting": ["asc", "desc"], "bSortable": true, "sClass": "right" },
      /* BRA */  { "asSorting": ["asc", "desc"], "bSortable": true, "sClass": "right" },
      /* WL */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* K */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* S */  { "asSorting": ["desc", "asc"], "bSortable": true, "sClass": "right" },
      /* Rank */  { "asSorting": ["asc"], "bSortable": true, "sClass": "right" },
      /* Role */  { "bVisible" : false },
      /* Rating */  { "asSorting": ["desc"], "bSortable": true, "sClass": "right",
        "fnRender": function ( oObj ) {
          return parseFloat(oObj.aData[9]).toFixed(2);
        } }
    ],
    "sAjaxSource": "json_unclaimedPitchers.php"
  } );
}