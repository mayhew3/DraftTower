/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/21/11
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */

function getLongName(stat, allStats) {
  var longCats = ["On-Base Percentage", "Slugging Percentage", "Runs - Home Runs", "Runs Batted In", "Home Runs",
    "Stolen Bases - Caught Stealing", "Innings Pitched", "Earned Runs", "Baserunners Allowed", "Wins - Losses", "Strikeouts (Pitcher)",
    "Saves"];
  for (var i=0; i<allStats.length;i++) {
    var cat = allStats[i];
    if (cat == stat) {
      return longCats[i];
    }
  }
  return "Unknown Category";
}

function createChart(chartCat, allCats) {
  return new Highcharts.Chart({
    chart: {
      renderTo: 'graphContainer'+chartCat,
      defaultSeriesType: 'bar'
    },
    title: {
      text: null
    },
    xAxis: {
      categories: [chartCat],
      opposite: true,
      title: {
        text: null
      }
    },
    yAxis: {
      min: 0,
      title: {
        text: null
      },
      labels: {
        enabled: false
      }
    },
    tooltip: {
      animation: false,
      formatter: function() {
        return getLongName(chartCat, allCats);
      }
    },
    plotOptions: {
      bar: {
        dataLabels: {
          enabled: true,
          formatter: function() {
            if (this.y < 1) {
              return truncateBAstyle(this.y);
            } else {
              return ''+this.y.toFixed(0);
            }
          }
        }
      }
    },
    legend: {
      enabled: false
    },
    credits: {
      enabled: false
    },
    series: [{
      name: 'Avg',
      data: []
    }, {
      name: 'You',
      data: []
    }]
  });

}
