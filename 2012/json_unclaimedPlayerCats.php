<?php
require_once "databases.php";
require_once "draftManager.php";
/*
	 * Script:    DataTables server-side script for PHP and MySQL
	 * Copyright: 2010 - Allan Jardine
	 * License:   GPL v2 or BSD (3-point)
	 */

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   * Easy set variables
   */

/* Array of database columns which should be read and sent back to DataTables. Use a space where
   * you want to insert a non-database field (for example a counter or static image)
   */
$aColumns = array( 'LastName', 'FirstName', 'MLBTeam',
          'Position', 'Eligibility',
          'OBP', 'SLG', 'RHR', 'RBI', 'HR', 'SBC',
          'INN', 'ERA', 'WHIP', 'WL', 'K', 'S',
          'Rank', 'Role', 'Total');

$whereClauses = array();

$whereClauses[] = "Year = 2012";

if (isset($_GET['pos'])) {
  $teamID = getMyTeamID();
  if (isset($_GET['commish']) && $_GET['commish'] == 1) {
    $teamID = getTeamCurrentlyUpInDraft();
  }
  $positions = getOpenPositionsFromDraftLineup($teamID);
  $posStr = "'" . implode("', '", $positions) . "'";
  $whereClauses[] = "Position IN ($posStr)";
}

/* Indexed column (used for fast and accurate table cardinality) */
$sIndexColumn = "PlayerID";

/* DB table to use */
$sTable = "UnclaimedDisplayPlayersWithCatsByQuality";

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   * If you just want to use the basic configuration for DataTables with PHP server-side, there is
   * no need to edit below this line
   */

/*
   * Paging
   */
$sLimit = "";
if ( isset( $_GET['iDisplayStart'] ) && $_GET['iDisplayLength'] != '-1' )
{
  $sLimit = "LIMIT ".mysql_real_escape_string( $_GET['iDisplayStart'] ).", ".
    mysql_real_escape_string( $_GET['iDisplayLength'] );
}


/*
   * Ordering
   */
if ( isset( $_GET['iSortCol_0'] ) )
{
  for ( $i=0 ; $i<intval( $_GET['iSortingCols'] ) ; $i++ ) {
    if ( $_GET[ 'bSortable_'.intval($_GET['iSortCol_'.$i]) ] == "true" ) {
      $orderingClauses[] = $aColumns[ intval( $_GET['iSortCol_'.$i] ) ]."
				 	".mysql_real_escape_string( $_GET['sSortDir_'.$i] );
    }
  }

  if ( empty($orderingClauses) ){
    $sOrder = "";
  } else {
    $sOrder = "ORDER BY " . implode(", ", $orderingClauses);
  }
}


/*
   * Filtering
   * NOTE this does not match the built-in DataTables filtering which does it
   * word by word on any field. It's possible to do here, but concerned about efficiency
   * on very large tables, and MySQL's regex functionality is very limited
   */

if ( $_GET['sSearch'] != "" )
{
  $orClauses = array();
  for ( $i=0 ; $i<count($aColumns) ; $i++ )
  {
    $orClauses[] = $aColumns[$i]." LIKE '%".mysql_real_escape_string( $_GET['sSearch'] )."%'";
  }
  $whereClauses[] = "(" . implode(" OR ", $orClauses) . ")";
}

/* Individual column filtering */
for ( $i=0 ; $i<count($aColumns) ; $i++ )
{
  if ( $_GET['bSearchable_'.$i] == "true" && $_GET['sSearch_'.$i] != '' )
  {
    $whereClauses[] = $aColumns[$i]." LIKE '%".mysql_real_escape_string($_GET['sSearch_'.$i])."%' ";
  }
}

//  $sWhere = "WHERE Year = 2012 AND Position = 'C'";
$sWhere = " WHERE " . implode(" AND ", $whereClauses) . " ";

/*
   * SQL queries
   * Get data to display
   */
$sQuery = "
		SELECT SQL_CALC_FOUND_ROWS ".str_replace(" , ", " ", implode(", ", $aColumns))."
		FROM   $sTable
		$sWhere
		$sOrder
		$sLimit
	";
$rResult = query( $sQuery );

/* Data set length after filtering */
$sQuery = "
		SELECT FOUND_ROWS()
	";
$rResultFilterTotal = query( $sQuery );
$aResultFilterTotal = mysql_fetch_array($rResultFilterTotal);
$iFilteredTotal = $aResultFilterTotal[0];

/* Total data set length */
$sQuery = "
		SELECT COUNT(".$sIndexColumn.")
		FROM   $sTable
	";
$rResultTotal = query( $sQuery );
$aResultTotal = mysql_fetch_array($rResultTotal);
$iTotal = $aResultTotal[0];


/*
   * Output
   */
$output = array(
  "sEcho" => intval($_GET['sEcho']),
  "iTotalRecords" => $iTotal,
  "iTotalDisplayRecords" => $iFilteredTotal,
  "aaData" => array()
);

while ( $aRow = mysql_fetch_array( $rResult ) )
{
  $row = array();
  for ( $i=0 ; $i<count($aColumns) ; $i++ )
  {
    if ( $aColumns[$i] == "version" )
    {
      /* Special output formatting for 'version' column */
      $row[] = ($aRow[ $aColumns[$i] ]=="0") ? '-' : $aRow[ $aColumns[$i] ];
    }
    else if ( $aColumns[$i] != ' ' )
    {
      /* General output */
      $row[] = $aRow[ $aColumns[$i] ];
    }
  }
  $output['aaData'][] = $row;
}

echo json_encode( $output );
?>
