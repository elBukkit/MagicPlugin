<?php

$metricsType = isset($_REQUEST['graph']) ? $_REQUEST['graph'] : "Rank";
$pluginName = isset($_REQUEST['plugin']) ? $_REQUEST['plugin'] : "Magic";
$width = isset($_REQUEST['width']) ? $_REQUEST['width'] : 1136;
$height = isset($_REQUEST['height']) ? $_REQUEST['height'] : 640;
$range = isset($_REQUEST['range']) ? $_REQUEST['range'] : 86400;

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
  <title><? echo "$pluginName $metricsType" ?></title>
  <style type="text/css">
    .timeBar {
      margin-left: 8px;
      margin-right: 8px;
    }
    .timeButton {
      padding: 0px;
    }
  </style>
  <script type="text/javascript" src="//www.google.com/jsapi"></script>
  <script type="text/javascript">
    var pluginName = '<?= $pluginName ?>';
    var metricsType = '<?= $metricsType ?>';
    var width = <?= $width ?>;
    var height = <?= $height ?>;
    var historySeconds = <?= $range ?>;

    var request = null;

    function loadGraph() {
        var metricsUrl = $metricsUrl = "http://api.mcstats.org/1.0/" + pluginName + "/graph/" + metricsType;
        request = new XMLHttpRequest;
        request.overrideMimeType("application/json");
        request.open('GET', metricsUrl, true);
        request.onreadystatechange = processResponse;
        request.send(null);
     }

    function processResponse() {
        if (request.readyState != 4) {
            return;
        }
        var response = JSON.parse(request.responseText);
        if (response == null || !('data' in response)) {
            alert("Invalid response: " + response);
            return;
        }

        var data = response['data'];
        var dataTable = new google.visualization.DataTable();
        dataTable.addColumn('date', 'Date');
        for (var dataField in data) {
           dataTable.addColumn('number', dataField);
        }

        var threshold = historySeconds > 0 ? (new Date()).getTime() - historySeconds * 1000 : 0;
        var timestamps = {};
        for (var dataField in data) {
           for (var index in data[dataField]) {
               var fieldData = data[dataField][index];
               var timestamp = fieldData[0];
               if (threshold > 0 && timestamp < threshold) continue;

               if (timestamp in timestamps) {
                 var timestampRecord = timestamps[timestamp];
                 timestampRecord[dataField] = fieldData[1];
                 timestamps[fieldData[0]] = timestampRecord;
               } else {
                 var newField = {};
                 newField[dataField] = fieldData[1];
                 timestamps[fieldData[0]] = newField;
               }
           }
        }

        var sortedTimestamps = Object.keys(timestamps).sort();
        var rows = [];
        for (var index in sortedTimestamps) {
          var timestamp = sortedTimestamps[index];
          var d = new Date();
          d.setTime(timestamp);
          var row = [d];
          for (var dataField in data) {
            row[row.length] = timestamps[timestamp][dataField];
          }
          rows[rows.length] = row;
        }
        // console.log(rows);
        dataTable.addRows(rows);

        var options = {
          title: pluginName + ' ' + metricsType,
          vAxis: {minValue: 0},
          chartArea: { left: 70, top: 20, width: width - 80, height: height - 40},
          colors: ['blue', 'green'],
          explorer: {}
        };
        var timeline = new google.visualization.AreaChart(document.getElementById('visualization'));
        timeline.draw(dataTable, options);
        document.getElementById("refreshButton").disabled = false;
    }

    function onRefresh()
    {
      var pluginSelector = document.getElementById("pluginSelector");
      var graphSelector = document.getElementById("graphSelector");
      pluginName = pluginSelector.value;
      metricsType = graphSelector.options[graphSelector.selectedIndex].value;

      document.getElementById("refreshButton").disabled = true;
      loadGraph();
    }

    function selectTime(timeSeconds)
    {
      historySeconds = timeSeconds;
      onRefresh();
    }

    google.load('visualization', '1', {packages: ['corechart']});
    google.setOnLoadCallback(loadGraph);
  </script>
</head>
<body style="font-family: Arial;border: 0 none;">
<div>
 <input id="pluginSelector" name="plugin" type="text" value="<?= $pluginName ?>"></input>
 <select id="graphSelector" name="graph" onchange="onRefresh()">
    <option <?php if ($metricsType == "Rank") echo ' selected="selected"'; ?>>Rank</option>
    <option <?php if ($metricsType == "Global Statistics") echo ' selected="selected"'; ?>>Global Statistics</option>
 </select>

 <span class="timeBar">
 <input id="twoHourButton" class="timeButton" type="button" value="2h" onclick="selectTime(7200);"></input>
 <input id="twelveHourButton" class="timeButton" type="button" value="12h" onclick="selectTime(43200);"></input>
 <input id="oneDayButton" class="timeButton" type="button" value="1d" onclick="selectTime(86400);"></input>
 <input id="twoDaysButton" class="timeButton" type="button" value="2d" onclick="selectTime(172800);"></input>
 <input id="oneWeekButton" class="timeButton" type="button" value="1w" onclick="selectTime(604800);"></input>
 <input id="allButton" class="timeButton" type="button" value="ALL" onclick="selectTime(0);"></input>
 </span>

 <input id="refreshButton" type="button" value="Refresh" onclick="onRefresh();"></input>
</div>
<div id="visualization" style="width: <?= $width ?>px; height: <?= $height ?>px;"></div>
</body>
</html>
​