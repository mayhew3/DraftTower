/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/21/11
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */

function getLongName(stat, allStats) {
  var longCats = ["On-Base Percentage", "Slugging Percentage", "Runs - Home Runs", "Runs Batted In", "Home Runs",
    "Stolen Bases - Caught Stealing", "Innings Pitched", "Earned Run Average", "Walks and Hits per Innings Pitched", "Wins - Losses", "Strikeouts (Pitcher)",
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
      categories: [chartCat + "<br>4th"],
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
            if (isNaN(this.y)) {
              return 0;
            } else if (this.y == 0) {
              return '0';
            } else if (this.y < 1) {
              return truncateBAstyle(this.y);
            } else if (chartCat == "ERA") {
              return truncateERAstyle(this.y);
            } else if (chartCat == "WHIP") {
              return truncateWHIPstyle(this.y);
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
      name: 'Average',
      data: []
    }, {
      name: 'You',
      data: []
    }]
  });

}
