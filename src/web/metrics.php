<?php

$metricsType = isset($_REQUEST['graph']) ? $_REQUEST['graph'] : "Rank";
$pluginName = isset($_REQUEST['plugin']) ? $_REQUEST['plugin'] : "Magic";
$width = isset($_REQUEST['width']) ? $_REQUEST['width'] : 1136;
$height = isset($_REQUEST['height']) ? $_REQUEST['height'] : 640;

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
  <title><? echo "$pluginName $metricsType" ?></title>
  <script type="text/javascript" src="//www.google.com/jsapi"></script>
  <script type="text/javascript">
    var pluginName = '<?= $pluginName ?>';
    var metricsType = '<?= $metricsType ?>';

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
           dataTable.addColumn('string', dataField + '-title');
           dataTable.addColumn('string', dataField + '-text');
        }
        var timestamps = {};
        for (var dataField in data) {
           for (var index in data[dataField]) {
               var fieldData = data[dataField][index];
               var timestamp = fieldData[0];
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
        var rows = [];
        for (var timestamp in timestamps) {
          var row = [new Date(timestamp / 1000)];
          for (var dataField in data) {
            row[row.length] = timestamps[timestamp][dataField];
            row[row.length] = null;
            row[row.length] = null;
          }
          rows[rows.length] = row;
        }
        console.log(rows);
        dataTable.addRows(rows);

        var timeline = new google.visualization.AnnotatedTimeLine(document.getElementById('visualization'));
        timeline.draw(dataTable, {'displayAnnotations': false});
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

    google.load('visualization', '1', {packages: ['annotatedtimeline']});
    google.setOnLoadCallback(loadGraph);
  </script>
</head>
<body style="font-family: Arial;border: 0 none;">
<div>
 <input id="pluginSelector" name="plugin" type="text" value="<?= $pluginName ?>"></input>
 <select id="graphSelector" name="graph">
    <option <?php if ($metricsType == "Rank") echo ' selected="selected"'; ?>>Rank</option>
    <option <?php if ($metricsType == "Global Statistics") echo ' selected="selected"'; ?>>Global Statistics</option>
 </select>
 <input id="refreshButton" type="button" value="Refresh" onclick="onRefresh();"></input>
</div>
<div id="visualization" style="width: <?= $width ?>px; height: <?= $height ?>px;"></div>
</body>
</html>
​