<?php
  require_once "databases.php";
  require_once "draftManager.php";
  CheckUser();

  $commish = 0;
  $graphAjaxUrl = "json_categoryAvgs.php";
  if (isset($_GET['commish'])) {
    CheckAdminUser();
    $commish = 1;
    $graphAjaxUrl .= "?commish=1";
  }


$categories = array("OBP", "SLG", "RHR", "RBI", "HR", "SBC", "INN", "ERA", "WHIP", "WL", "K", "S");
?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Uncharted Territories - Draft Wizard</title>

  <style type="text/css" title="currentStyle">
			@import "datatables/css/unchartedPage.css";
			@import "datatables/css/unchartedTable.css";
			@import "datatables/themes/smoothness/jquery-ui-1.8.4.custom.css";

      #radioPlayers {
        font-size:smaller;
      }

      #tablesContainer {
        float: left;
        width: 100%;
      }
      #overview {
        float: left;
        width: 20%;
      }
      #pitcherBatter {
        float: left;
        width: 60%;
      }
      #graphs {
        float:left;
        width: 251px;
      }
      .openPos {
        background-color: #DBFF70;
      };

      .ui-button-text-only .ui-button-text { padding: .2em .5em; }
		</style>

  <!-- load datatables and bar chart -->
  <script src="datatables/js/jquery.js" type="text/javascript"></script>
  <script src="datatables/js/jquery.dataTables.js" type="text/javascript"></script>
  <script src="highcharts/js/highcharts.js" type="text/javascript"></script>
  <script src="js/unchartedDataTables.js" type="text/javascript"></script>
  <script src="js/unchartedBarChart.js" type="text/javascript"></script>
  <script src="datatables/themes/smoothness/jquery-ui-1.8.11.custom.min.js" type="text/javascript"></script>

  <!-- datatables code -->
  <script type="text/javascript" charset="utf-8">
    var resultsTable;
    var myTeamTable;
    var playerCatTable;

    var charts = [];
    var chartCats = ["OBP", "SLG", "RHR", "RBI", "HR", "SBC", "INN", "ERA", "WHIP", "WL", "K", "S"];

    var positions = ['C', '1B', '2B', '3B', 'SS', 'OF', 'DH', 'P'];

    var rankings = ['0th', '1st', '2nd', '3rd', '4th', '5th', '6th', '7th', '8th', '9th', '10th', '11th', '12th'];

    var openPositions = false;

    $(document).ready(function() {
      resultsTable = createResultsTable($("#results"));
      myTeamTable = createMyTeamTable($("#myteam"), <?=$commish?>);
      playerCatTable = createPlayerCatTable($("#playerCats"), getData, <?=$commish?>);

      for (var i=0; i<chartCats.length; i++) {
        charts[i] = createChart(chartCats[i], chartCats);
      }
      refreshGraphData();

      preparePlayerButtonSet();
    } );

    function getData( sSource, aoData, fnCallback ) {
      /* Add some data to send to the source, and send as 'POST' */
      if (openPositions) {
        aoData.push( { "name": "pos", "value": 1 } );
      }
      $.ajax( {
        "dataType": 'json',
        "type": "GET",
        "url": sSource,
        "data": aoData,
        "success": fnCallback
      } );
    }

    function preparePlayerButtonSet() {
      var radioString = '<div id="radioPlayers">' +
          '<input type="radio" id="radioPlayersAll" name="radioPlayers" checked="checked" /><label for="radioPlayersAll">All</label>';
      for (var i = 0; i < positions.length; i++) {
        position = positions[i];
        radioString += '<input type="radio" id="' + position + '" name="radioPlayers"/><label for="' + position + '">' + position + '</label>';
      }
      radioString += '<input type="radio" id="sp" name="radioPlayers"/><label for="sp">Starters</label>';
      radioString += '<input type="radio" id="rp" name="radioPlayers"/><label for="rp">Closers</label>';
      radioString += '<input type="radio" id="radioOpen" name="radioPlayers"/><label for="radioOpen">Unfilled</label>';
      radioString += '</div>';

      $("#RG").html(radioString);

      $(function() {
        $( "#radioPlayers" ).buttonset();

        $("#radioPlayersAll").button();
        $("#radioPlayersAll").click(function() {
          openPositions = false;
          playerCatTable.fnFilter( "", 18 );
          playerCatTable.fnDraw(false);
          playerCatTable.fnFilter( "", 3 );
        });

        $("#radioOpen").button();
        $("#radioOpen").click(function() {
          openPositions = true;
          playerCatTable.fnFilter( "", 18 );
          playerCatTable.fnDraw(false);
          playerCatTable.fnFilter("", 3);
        });

        $("#sp").button();
        $("#sp").click(function() {
          playerCatTable.fnFilter( "", 3 );
          playerCatTable.fnFilter("Starter", 18);
        });

        $("#rp").button();
        $("#rp").click(function() {
          playerCatTable.fnFilter( "", 3 );
          playerCatTable.fnFilter("Closer", 18);
        });

        for (var i = 0; i < positions.length; i++) {
          var buttonName = "#" + positions[i];
          $(buttonName).button();
          $(buttonName).click(function() {
            var position = this.id;
            openPositions = false;
            playerCatTable.fnFilter( "", 18 );
            playerCatTable.fnDraw(false);
            playerCatTable.fnFilter(position, 3);
          });
        }
      });
    }


    function refreshAllData() {
      resultsTable.fnDraw(false);
      myTeamTable.fnDraw(false);
      playerCatTable.fnDraw(false);
      refreshGraphData();
    }


    function refreshGraphData() {
    $.ajax({
        url: '<?=$graphAjaxUrl?>',
        success: function(result) {
          for (var i=0; i < chartCats.length; i++) {
            var dataPair = result[i];
            charts[i].xAxis[0].setCategories([chartCats[i] + "<br>" + rankings[dataPair[2]]]);
            charts[i].series[0].setData([parseFloat(dataPair[0])]);
            charts[i].series[1].setData([parseFloat(dataPair[1])]);
          }
        },
        cache: false
    });
}
    setInterval(refreshAllData, 5000);
  </script>

</head>

<body id="unchartedBody">



  <div id="tablesContainer">
    <div id="graphs">
      <img src="images/chartLegend.jpg" alt="(Red: You, Blue: Avg)">  <br>
    <? foreach ($categories as $category) { ?>
    <div id="graphContainer<?=$category?>" style="width: 250px; height: 70px; margin: 0 auto;"></div>
    <? } ?>
  </div>

    <div id="pitcherBatter">

      <b>Players:</b><br>
      <table cellpadding="0" cellspacing="0" border="0" class="display" id="playerCats">
        <thead>
        <tr>
          <th>LastName</th>
          <th>FirstName</th>
          <th>MLB</th>
          <th>Pos</th>
          <th>Elig</th>
          <th>OBP</th>
          <th>SLG</th>
          <th>R+</th>
          <th>RBI</th>
          <th>HR</th>
          <th>SBC</th>
          <th>INN</th>
          <th>ERA</th>
          <th>WHIP</th>
          <th>WL</th>
          <th>K</th>
          <th>S</th>
          <th>Rank</th>
          <th>Role</th>
          <th>Rating</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td colspan="20" class="dataTables_empty">Loading data from server</td>
        </tr>
        </tbody>
      </table>

    </div>
      <div id="overview">

        <b>Results:</b>

        <table cellpadding="0" cellspacing="0" border="0" class="display" id="results">
          <thead>
          <tr>
            <th>ID</th>
            <th>Rd</th>
            <th>Pk</th>
            <th>Team</th>
            <th>Pos</th>
            <th>Player</th>
            <th>Keeper</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td colspan="6" class="dataTables_empty">Loading data from server</td>
          </tr>
          </tbody>
        </table>

        <b>My Team:</b>
        <table cellpadding="0" cellspacing="0" border="0" class="display" id="myteam">
          <thead>
          <tr>
            <th>Pos</th>
            <th>Player</th>
            <th>IsOpen</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td colspan="3" class="dataTables_empty">Loading data from server</td>
          </tr>
          </tbody>
        </table>


    </div> <!-- close left div -->


  </div>

</body>
</html>
