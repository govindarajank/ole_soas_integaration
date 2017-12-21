<%@ page import="java.util.Date" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Kuali Portal Index</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="keywords" content="jquery,ui,easy,easyui,web">
    <meta name="description" content="easyui help you build your web page easily!">

    <link rel="stylesheet" type="text/css" href="css/easyui.css">
    <link rel="stylesheet" type="text/css" href="css/icon.css">
    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
    <link type="text/css" rel="Stylesheet"
          href="css/jquery-ui-1.8.16.custom.css"/>

    <%--header start--%>
    <link href="./css/olePortal.css" rel="stylesheet" type="text/css"/>

    <style type="text/css">
        .center {
            margin-left: auto;
            margin-right: auto;
            width: 80%;
            top:120px;
        }
        .middle{
            position:absolute;
            right:900px;
            top:120px;
        }
        .Fines{
            position:absolute;
            right: 400px;
            top:120px;
        }
        p {
            font-size: 125%;
        }
        h2{
            font-size:250%;
        }
        h5 {
            font-size: 200%;
        }
        li{
            font-size: 150%;
        }

        #statistics{
            top:150px;
            position:absolute;
            width:200px;
        }

    </style>

</head>
<body>

<div id="header" title="Kuali Open Library Environment">
    <h1 class="kfs"></h1>
</div>
<div id="feedback">
    <a class="portal_link" href="#"
       title="Provide Feedback">Provide Feedback</a>
</div>
<div id="build"><%=new Date()%>
</div>
<div id="tabs" class="tabposition">
    <ul>
        <li class="red"><a class="red" href="." title="Main Menu" onclick="show()">Reports</a></li>
    </ul>
</div>
<div class="header2">
    <div class="header2-left-focus">
        <div class="breadcrumb-focus">

        </div>
    </div>
</div>
<div id="iframe_portlet_container_div">
    <br/>
    <div class="center">
        <%   if (request != null && request.getRequestURL() != null && request.getRequestURL().toString().contains("reports.staging.ole.kuali.org")) {%>
        <p>Note: The reports data source is pointing to the staging environment at the moment.</p>
        <% } %>
        <ul class="first">
            <h5><u>Requests</u></h5>
            <ul>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/CancelledOnHolds.rptdesign"%>" target="_blank">Cancelled On Holds</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/Holds.rptdesign"%>" target="_blank">Requests List</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/ItemMostlyRequested.rptdesign"%>" target="_blank">Item Requested Most</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/RequestedOverdueItems.rptdesign"%>" target="_blank">Requested Item Overdue</a></li>
            </ul>
        </ul>
        <ul class="first">
            <div class ="Fines">

                <h5><u>Fines</u></h5>
                <ul>
                    <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/CashTransactions.rptdesign"%>" target="_blank">Cash Transactions</a></li>
                    <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/VufindLogs.rptdesign"%>" target="_blank">Vufind Fines</a></li>
                </ul>

            </div>
        </ul>
    </div>
    <div class="middle">
        <ul class="first">
            <h5><u>History</u></h5>
            <ul>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/PatronList.rptdesign"%>" target="_blank">Patron List</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/PatronNotes.rptdesign"%>" target="_blank">Patron Notes</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/FastAdd.rptdesign"%>" target="_blank">FastAdd</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/ItemCirculationRecord.rptdesign"%>" target="_blank">Item Circulation History</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/CirculationHistoryItems.rptdesign"%>" target="_blank">Circulation History List</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/PatronCirculationHistory.rptdesign"%>" target="_blank">Patron Circulation History</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/ClaimsReturn.rptdesign"%>" target="_blank">Claims Returned</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/LostOrMissingItems.rptdesign"%>" target="_blank">Lost/Missing Items</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/GeneralStatistics.rptdesign"%>" target="_blank">General Statistics</a></li>
            </ul>
        </ul>
        <%--<ul id="statistics">
            <h5><u>Statistics</u></h5>
            <ul>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/ItemTypeStatistics.rptdesign"%>" target="_blank">Item Type Statistics</a></li>
                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/CollectionStatistics.rptdesign"%>" target="_blank">Collection Statistics</a></li>

                <li><a href="<%= request.getContextPath() + "/frameset?__report=deliver/StandardLoanBooks.rptdesign"%>" target="_blank">Standard Loan Books</a></li>
            </ul>
        </ul>--%>

    </div>
    <br/><br/><br/><br/>
</div>
<%@ include file="oleFooter.jsp" %>
</body>
</html>
