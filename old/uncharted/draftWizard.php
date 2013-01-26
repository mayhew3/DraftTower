<?php
  require_once "databases.php";
  CheckUser();

  $categories = array("OBP", "SLG", "RHR", "RBI", "HR", "SBC", "INN", "ER", "BRA", "WL", "K", "S");
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

      #radioBatter {
        font-size:smaller;
      }
      #radioPitcher {
        font-size:smaller;
      }

      #tablesContainer {
        float: left;
        width: 100%;
      }
      #overview {
        float: left;
        width: 25%;
      }
      #pitcherBatter {
        float: left;
        width: 50%;
      }
      #graphs {
        float:left;
        width: 251px;
      }
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
    var unclaimedTable;
    var batterTable;
    var pitcherTable;

    var charts = [];
    var chartCats = ["OBP", "SLG", "RHR", "RBI", "HR", "SBC", "INN", "ER", "BRA", "WL", "K", "S"];

    var positions = ['C', '1B', '2B', '3B', 'SS', 'OF'];

    var openPositions = false;

    $(document).ready(function() {
      resultsTable = createResultsTable($("#results"));
      unclaimedTable = createUnclaimedTable($("#unclaimedPlayers"));
      batterTable = createBatterTable($("#batters"), getData);
      pitcherTable = createPitcherTable($("#pitchers"));

//      batterTable.fnFilter("DH", 10);

      for (var i=0; i<chartCats.length; i++) {
        charts[i] = createChart(chartCats[i], chartCats);
      }
      refreshGraphData();

      prepareHitterButtonSet();
      preparePitcherButtonSet();
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

    function prepareHitterButtonSet() {
      var radioString = '<div id="radioBatter">' +
          '<input type="radio" id="radioBatterAll" name="radioBatter" checked="checked" /><label for="radioBatterAll">All</label>';
      for (var i = 0; i < positions.length; i++) {
        position = positions[i];
        radioString += '<input type="radio" id="' + position + '" name="radioBatter"/><label for="' + position + '">' + position + '</label>';
      }
      radioString += '<input type="radio" id="radioOpen" name="radioBatter"/><label for="radioOpen">Unfilled</label>';
      radioString += '</div>';

      $("#RG").html(radioString);

      $(function() {
        $( "#radioBatter" ).buttonset();

        $("#radioBatterAll").button();
        $("#radioBatterAll").click(function() {
          openPositions = false;
          batterTable.fnDraw(false);
          batterTable.fnFilter( "", 1 );
        });

        $("#radioOpen").button();
        $("#radioOpen").click(function() {
          openPositions = true;
          batterTable.fnDraw(false);
          batterTable.fnFilter("", 1);
        });

        for (var i = 0; i < positions.length; i++) {
          var buttonName = "#" + positions[i];
          $(buttonName).button();
          $(buttonName).click(function() {
            var position = this.id;
            openPositions = false;
            batterTable.fnDraw(false);
            batterTable.fnFilter(position, 1);
          });
        }
      });
    }


    function preparePitcherButtonSet() {
      var radioString = '<div id="radioPitcher">' +
          '<input type="radio" id="radioPitcherAll" name="radioPitcher" checked="checked" /><label for="radioPitcherAll">All</label>';

      radioString += '<input type="radio" id="sp" name="radioPitcher"/><label for="sp">Starters</label>';
      radioString += '<input type="radio" id="rp" name="radioPitcher"/><label for="rp">Closers</label>';
      radioString += '</div>';

      $("#RP").html(radioString);

      $(function() {
        $( "#radioPitcher" ).buttonset();

        $("#radioPitcherAll").button();
        $("#radioPitcherAll").click(function() {
          pitcherTable.fnFilter( "", 8 );
        });

        $("#sp").button();
        $("#sp").click(function() {
          pitcherTable.fnFilter("Starter", 8);
        });

        $("#rp").button();
        $("#rp").click(function() {
          pitcherTable.fnFilter("Closer", 8);
        });
      });
    }

    function refreshAllData() {
      resultsTable.fnDraw(false);
      unclaimedTable.fnDraw(false);
      batterTable.fnDraw(false);
      pitcherTable.fnDraw(false);
      refreshGraphData();
    }


    function refreshGraphData() {
    $.ajax({
        url: 'json_categoryAvgs.php',
        success: function(result) {
          for (var i=0; i < chartCats.length; i++) {
            var dataPair = result[i];
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

      <span class="subHeader">Hitters:</span><br>
      <table cellpadding="0" cellspacing="0" border="0" class="display" id="batters">
        <thead>
        <tr>
          <th>Player</th>
          <th>Pos</th>
          <th>Elig</th>
          <th>OBP</th>
          <th>SLG</th>
          <th>R+</th>
          <th>RBI</th>
          <th>HR</th>
          <th>SBC</th>
          <th>Rank</th>
          <th>Rating</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td colspan="11" class="dataTables_empty">Loading data from server</td>
        </tr>
        </tbody>
      </table>

      <br>
      <span class="subHeader">Pitchers:</span><br>
      <table cellpadding="0" cellspacing="0" border="0" class="display" id="pitchers">
        <thead>
        <tr>
          <th>Player</th>
          <th>INN</th>
          <th>ER</th>
          <th>BRA</th>
          <th>W+</th>
          <th>K</th>
          <th>S</th>
          <th>Rank</th>
          <th>Role</th>
          <th>Rating</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td colspan="10" class="dataTables_empty">Loading data from server</td>
        </tr>
        </tbody>

      </table>
    </div>
      <div id="overview">

        Results:

        <table cellpadding="0" cellspacing="0" border="0" class="display" id="results">
          <thead>
          <tr>
            <th>ID</th>
            <th>Rd</th>
            <th>Pk</th>
            <th>Team</th>
            <th>Player</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td colspan="5" class="dataTables_empty">Loading data from server</td>
          </tr>
          </tbody>
        </table>
        Unclaimed Players:<br>
        <table cellpadding="0" cellspacing="0" border="0" class="display" id="unclaimedPlayers">
          <thead>
          <tr>
            <th>Player</th>
            <th>Pos</th>
            <th>Rank</th>
            <th>Rating</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td colspan="4" class="dataTables_empty">Loading data from server</td>
          </tr>
          </tbody>

        </table>

    </div> <!-- close left div -->


  </div>

</body>
</html>
