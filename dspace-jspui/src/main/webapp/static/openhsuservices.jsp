<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Serviceseite openHSU</title>
  <!--<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
  <link rel="stylesheet" href="/resources/demos/style.css"> -->
  
  <!-- Stylesheet fuer die Tabs und Akkordeons; entspricht bearbeiteter Version des jQuery UI-CSS (siehe Z. 22); A.G., 13.05.2018  -->
  <link  href="/static/css/jqueryui_custom.css" rel="stylesheet" />
  
  <!-- Stylesheet fuer eigene Styles; A.G., 13.05.2018 -->
  <link href="/static/css/servicepage_custom.css" rel="stylesheet">
  
  
  <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
  <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  
  
		<!-- statt auskommentiertem Stylesheet in naechster Zeile oben eingebundenes customtheme2_update.css  fuer Tabs und Akkordeons; s.o. -->
		<!-- <link rel="stylesheet" href="/static/css/jquery-ui-1.10.3.custom/redmond/jquery-ui-1.10.3.custom.css" type="text/css" /> -->
	    <link href="/css/researcher.css" type="text/css" rel="stylesheet" />
		<link href="/css/jdyna.css" type="text/css" rel="stylesheet" />
	    <link rel="stylesheet" href="/static/css/bootstrap/bootstrap.min.css" type="text/css" />
	    <link rel="stylesheet" href="/static/css/bootstrap/bootstrap-theme.min.css" type="text/css" />
	    <link href="/static/css/font-awesome/css/font-awesome.min.css" rel="stylesheet">
		<link href="/static/css/jstree/themes/default/style.min.css" rel="stylesheet"/>
	    <link rel="stylesheet" href="/static/css/bootstrap/dspace-theme.css" type="text/css" />
	    <link rel="stylesheet" href="/css/orcid.css" type="text/css" />
	    <link rel="stylesheet" type="text/css" href="/static/css/dataTables.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="/static/css/buttons.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="/static/css/responsive.bootstrap.min.css"/>
		<link rel="stylesheet" href="/static/css/bootstrap/dspace-theme.css" type="text/css" />
		<link rel="stylesheet" type="text/css" href="/css/bootstrap-datetimepicker.min.css" />

        <link rel="alternate" type="application/rdf+xml" title="Items in openHSU" href="/feed/rss_1.0/site"/>

        <link rel="alternate" type="application/rss+xml" title="Items in openHSU" href="/feed/rss_2.0/site"/>

        <link rel="alternate" type="application/rss+xml" title="Items in openHSU" href="/feed/atom_1.0/site"/>

        <link rel="search" type="application/opensearchdescription+xml" href="/open-search/description.xml" title="DSpace"/>

        
   	<script type='text/javascript' src="/static/js/jquery/jquery-1.11.3.min.js"></script>
	<script type='text/javascript' src='/static/js/jquery/jquery-ui-1.11.4.min.js'></script>
	<script type="text/javascript" src="/js/moment.js"></script>
	<script type='text/javascript' src='/static/js/bootstrap/bootstrap.min.js'></script>
	<script type='text/javascript' src='/static/js/holder.js'></script>
	<script type="text/javascript" src="/utils.js"></script>
	<script type='text/javascript' src='/static/js/custom-functions.js'></script>
    <script type="text/javascript" src="/static/js/choice-support.js"> </script>
    <script type="text/javascript" src="/js/jdyna/jdyna.js"></script>    
    <script type="text/javascript" src="/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="/js/dataTables.bootstrap.min.js"></script>
	<script type="text/javascript" src="/js/dataTables.buttons.min.js"></script>
	<script type="text/javascript" src="/js/buttons.bootstrap.min.js"></script>
	<script type="text/javascript" src="/js/buttons.html5.min.js"></script>
	<script type="text/javascript" src="/js/dataTables.responsive.min.js"></script>	
	<script type="text/javascript" src="/js/bootstrap-datetimepicker.min.js"></script>
	<script type="text/javascript" src="/js/jszip.min.js"></script>
	<script type='text/javascript'>
		var j = jQuery.noConflict();
		var $ = jQuery.noConflict();
		var JQ = j;
		dspaceContextPath = "";
		
		<!-- tooltip-Funktion wird u.a. genutzt fuer back to top-Button; A.G., 13.05.2018  -->
		jQuery(document).ready(function ($) {
			  $('span[data-toggle="tooltip"]').tooltip();
			  $('i[data-toggle="tooltip"]').tooltip();
		});
	</script>
	
    
	

	<!-- HTML5 shiv and Respond.js IE8 support of HTML5 elements and media queries -->
	<!--[if lt IE 9]>  
	  <script src="/static/js/html5shiv.js"></script>
	  <script src="/static/js/selectivizr-min.js"></script>
	  <script src="/static/js/respond.min.js"></script>
	  <link rel="stylesheet" href="/static/css/bootstrap/dspace-theme-IElte9.css" type="text/css" />
	<![endif]-->
   
   
   
   
   
<!-- back to top Button; Quelle: https://bootsnipp.com/snippets/featured/link-to-top-page; A.G., 13.05.2018  -->
<!-- untenstehender Code wird gar nicht benoetigt; der Code sollte auch nicht benutzt werden, da er sonst
die Tabs und Akkordeons "ueberschreibt"  -->
<!-- es genuegt die tooltip-Funktion oben  -->

<!--<link href="//netdna.bootstrapcdn.com/bootstrap/3.1.0/css/bootstrap.min.css" rel="stylesheet" id="bootstrap-css">
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.0/js/bootstrap.min.js"></script>
<script src="//code.jquery.com/jquery-1.11.1.min.js"></script>



<script>
$(document).ready(function(){
     $(window).scroll(function () {
            if ($(this).scrollTop() > 50) {
                $('#back-to-top').fadeIn();
            } else {
                $('#back-to-top').fadeOut();
            }
        });
        // scroll body to 0px on click
        $('#back-to-top').click(function () {
            $('#back-to-top').tooltip('hide');
            $('body,html').animate({
                scrollTop: 0
            }, 800);
            return false;
        });
        
        $('#back-to-top').tooltip('show');

});

</script> -->



   
   
<!-- Funktion 1-6 mit window.location.href in Kombination mit location.reload; dann wird die angegebene URL geladen und
somit das gewuensche Tab; A.G., 13.05.2018  -->

   <!-- https://www.w3schools.com/jsref/met_loc_reload.asp  -->
   <!-- https://appendto.com/2016/04/javascript-redirect-how-to-redirect-a-web-page-with-javascript/  -->
<!--<script>
function myFunction() {
    location.reload();
}
</script> -->



<script>
function myFunction1() {
    window.location.href = "openhsuservices.jsp#tabs-1";
	{
		location.reload();
	}
}
</script>


<script>
function myFunction2() {
    window.location.href = "openhsuservices.jsp#tabs-2";
	{
		location.reload();
	}
}
</script>


<script>
function myFunction3() {
    window.location.href = "openhsuservices.jsp#tabs-3";
	{
		location.reload();
	}
}
</script>

<script>
function myFunction4() {
    window.location.href = "openhsuservices.jsp#tabs-4";
	{
		location.reload();
	}
}
</script>

<script>
function myFunction5() {
    window.location.href = "openhsuservices.jsp#tabs-5";
	{
		location.reload();
	}
}
</script>

<script>
function myFunction6() {
    window.location.href = "openhsuservices.jsp#tabs-6";
	{
		location.reload();
	}
}
</script>

<script>
function myFunction6() {
    window.location.href = "openhsuservices.jsp#tabs-6";
        {
                location.reload();
        }
}
</script>




<script>
function myFunction6() {
    window.location.href = "openhsuservices.jsp#tabs-7";
        {
                location.reload();
        }
}
</script>





<!-- redirect f?r jedes Tab; A.G., 13.05.2018  -->
<!--<script>
// onclick event is assigned to the #button element.
document.getElementById("button").onclick = function() {
  window.location.href = "https://www.example.com";
};
</script>-->



<!-- Aufruf der Funktionen fuer jQuery-UI Widgets Tabs und Akkordeon; verkn?pftes Stylesheet enthaelt jQueryUI -> jqueryui_custom.css (Original siehe Z. 22); A.G., 13.05.2018  -->
	<script>
  $( function() {
    $( "#tabs" ).tabs();
  } );
  </script>
  
	
  <script>
  $( function() {
    $( "#accordion" ).accordion();
  } );
  </script>
  
  
   <script>
  $( function() {
    $( "#accordion1" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>	

  
   <script>
  $( function() {
    $( "#accordion2" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>	 
  
  
    <script>
  $( function() {
    $( "#accordion3" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>	 
  
  
  <script>
  $( function() {
    $( "#accordion4" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>	 
  
  
  
   <script>
  $( function() {
    $( "#accordion5" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>	
  
  
  
  
   <script>
  $( function() {
    $( "#accordion6" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>	
  



  <script>
  $( function() {
    $( "#accordion7" ).accordion({
      heightStyle: "content"
    });
  } );
  </script>



<script>
$('#myCarousel').carousel()
</script> 



<script>
$('#myCarousel').carousel({interval:3000})
</script> 



</head>


 <!-- body-tag eingefuegt; A.G., 11.05.2018  -->
<body>

<!-- Navigationsleiste und Header; A.G., 06.05.2018  --> 
 
<div id="header">
<!-- Einbindung der Navigationsleiste und des Header-Bereichs; A.G., 11.05.2018  -->
<header class = "navbar navbar-inverse navbar-square">

<!--<style>

<!-- Navbar dunkelgrau, da Navbar sonst wie background hellgrau ist; A.G., 11.05.2018  -->
<!--.navbar-inverse {
	background-image: linear-gradient(to bottom,#3c3c3c 0,#222 100%);
}

</style>-->

	<jsp:include page="navbar-default.jsp"/> 
<!--</header>-->

<!-- Test, ob include header moeglich ist im Verzeichnis /layout/openhsuservices2.jsp; es kam eine Fehlermeldung im Verzeichnis /static;
ggf. funktioniert dann auch der Tab-Aufruf auf der Serviceseite ohne Seite neu laden zu muessen; A.G., 11.05.2018  -->
<!-- theoretisch muesste es funktionieren, weil f?r die Startseite der gleiche header genutzt wird; A.G., 11.05.2018  -->
<!-- UPDATE: header-default.jsp einbinden beim Abspeichern der openhsuservices2.jsp in /layout gibt Systemfehler; A.G., 11.05.2018  -->

<!-- verirrtes div-end-tag; A.G., 13.05.2018  -->
<!--</div>-->

<!-- div-Container eingebunden fuer Header-Bereich; A.G., 12.05.2018  -->
<!-- TODO: fuer .jsp-Version ebenso einbinden; A.G., 12.05.2018  -->

<!-- schlie?endes div-tag zwecks Test Navbar; A.G., 14.05.2018  -->
</header>
   </div>
   
   
<main id="content" role="main">
<div class="container banner">
	<div class="row"> 
		
		  <div class="col-sm-8 brand pull-left">
		<!-- <h4>Services<small> kompakt</small></h4>  -->
<!-- <h4>Willkommen in openHSU, dem Forschungsinformationssystem, Open Access-Repository und Portal der Universit&aauml;tsbibliografie der Helmut-Schmidt-Universit?t/ Universit?t der Bundeswehr Hamburg. Die hier enthaltenen Publikationen stehen Ihnen unter Ber?cksichtigung der jeweiligen Lizenz und des geltenden Urheberrechts kostenlos zur Verf?gung. </h4><!--<a href="http://cineca.github.io/dspace-crisdspace-cris/" class="btn btn-primary">Erfahren Sie mehr. <i class="fa fa-external-link"></i></a>--> 
<img class="img-responsive" src="/image/serviceseite.gif" a href="https://unsplash.com/photos/0FRJ2SCuY4k" width="250" alt="Bildquelle: https://unsplash.com/photos/0FRJ2SCuY4k"/>

<!-- <style>
.img-responsive {
padding-top:20px;
}
</style>-->


</div>
<div class="col-sm-4 hidden-xs pull-right"><img class="img-responsive" src="/image/logo.gif" alt="DSpace logo" />
</div>
</div>
</div>	
<br/>

<!-- schlie?endes header-tag zwecks Test Navbar; A:G., 14.05.2018  -->
<!-- </header>  -->


<div class="container fullheight">




<!-- Beginn der Tabs und Akkordeons mit jQueryUI; Stylesheet customtheme2_update.css; A.G., 06.05.2018  -->
<div class="container-fluid">
<div class="row">
<div class="col-sm-12">




<div id="tabs">

<!-- Tabs-Liste  -->
<ul>
<li><a href="#tabs-1">openHSU LEITLINIEN</a></li>
<li><a href="#tabs-2">MEIN OPENHSU</a></li>
<li><a href="#tabs-3">MEIN PROFIL</a></li>
<li><a href="#tabs-4">MEINE PUBLIKATIONSLISTE</a></li>
<li><a href="#tabs-5">PUBLIZIEREN</a></li>
<li><a href="#tabs-6">ORCID</a></li>
<li><a href="#tabs-7">NEWS</a></li>
</ul>


<!-- openHSU-Leitlinien-Tab  -->
<div id="tabs-1"> <!--class="ui-tabs-panel ui-widget-content ui-corner-bottom" style="background:none"-->
<div id="accordion1">

<h3>openHSU Leitlinien</h3>
<div>

<div class="jumbotron" style="background-color:#466675"; "display: inline-block"; "position: relative"; "width: 100%"; "background-size: contain">
<!--
<div class="jumbotron" style="background-color:#466675">
-->
<p style="color:#EDEBE7">openHSU speichert digitales Material aus Forschung und Lehre. Es sorgt f&uuml;r dessen Erhalt, Erschlie&szlig;ung und Verbreitung.</p>
<p style="color:#EDEBE7">Sie finden hier Dissertationen, Technische Reports, Konferenzberichte,
Zeitschriftenartikel, Vorlesungsskripte u.v.m. aus allen Bereichen der Universit&auml;t.</p>	
<p style="color:#EDEBE7">Der Inhalt in openHSU ist analog zur <a href="https://openhsu.ub.hsu-hh.de/cris/explore/orgunits" style="color: #CFE107">Universit&auml;tsstruktur</a> organisiert. In den beiden Sammlungen &quot;Publications of the HSU Researchers&quot; und &quot;Publications of the HSU Researchers (before HSU)&quot; finden Sie Open Access-Ver&ouml;ffentlichungen und Publikations-Metadaten.</p>
</div>



</br>


<!-- 
<div id="myCarousel" class="carousel slide">

<ol class="carousel-indicators">

	<li data-target="#myCarousel" data-slide-to="0" class="active"></li>
	<li data-target="#myCarousel" data-slide-to="1"></li>
	<li data-target="#myCarousel" data-slide-to="2"></li>

	<li data-target="#myCarousel" data-slide-to="3"></li>			

	<li data-target="#myCarousel" data-slide-to="4"></li>

</ol>
-->
<!-- Carousel items -->
<!--

<div class="carousel-inner">

<div class="active item"><img src="/image/item-1.jpg" alt="Serviceseite" class="img-responsive center-block" ></div>

<div class="item" ><img src="/image/item-3.jpg" alt="Conferences" class="img-responsive center-block"></div>

<div class="item"><img src="/image/card-2.jpg" alt="Organizations" class="img-responsive center-block"></div>

<div class="item"><img src="/image/item.3.jpg alt="Card 3" class="img-responsive center-block"></div>

<div class="item"><img src="/image/item-3.jpg" alt="Item 3" class="img-responsive center-block"></div>

</div>
-->

<!-- Carousel nav -->
<!--
<a class="carousel-control left" href="#myCarousel" data-slide="prev">
<span class="glyphicon glyphicon-chevron-left"></span>

</a>

<a class="carousel-control right" href="#myCarousel" data-slide="next">
<span class="glyphicon glyphicon-chevron-right"></span>

</a>


</div>
-->

</div>


<h3>Ziele und inhaltliche Kriterien</h3>
<div>
<p>Die Universit&auml;tsbibliothek der Helmut-Schmidt-Universit&auml;t/ Universit&auml;t der Bundeswehr Hamburg (HSU/ UniBw H) bietet mit dem Forschungsinformationssystem openHSU allen Angeh&ouml;rigen der Universit&auml;t die organisatorische und technische Infrastruktur zur kostenlosen Ver&ouml;ffentlichung ihrer wissenschaftlichen Publikationen. Im Rahmen dieses Angebots werden wissenschaftliche Dokumente im Internet f&uuml;r Forschung und Lehre bereitgestellt.
</p>
<p>
openHSU wird inhaltlich und technisch durch die Universit&auml;tsbibliothek betreut. Die enthaltenen elektronischen Publikationen werden durch entsprechende Metadatenfelder im Upload-Formular mit qualifizierten Beschreibungen erschlossen und &uuml;ber das System bereitgestellt. Zudem wird die Langzeitarchivierung angestrebt. Die Archivierungsdauer h&auml;ngt jedoch von der Verf&uuml;gbarkeit des Formates, der Betrachtungssoftware sowie den Konvertierungsm&ouml;glichkeiten ab. Die UB garantiert jedoch eine Archivierung von 5 Jahren. S&auml;mtliche Inhalte in openHSU sind &uuml;ber nationale und internationale Bibliothekskataloge, Suchmaschinen sowie andere Nachweisinstrumente recherchierbar und weltweit frei zug&auml;nglich. Die UB vergibt f&uuml;r die Zitation sowie den f&uuml;r die Langzeitarchivierung notwendigen persistenten Identifikator einen DOI (Digital Object Identifier). Somit enthalten auch s&auml;mtliche an die Deutsche Nationalbibliothek gemeldeten Dokumente diesen DOI, der von der Deutschen Nationalbibliothek verzeichnet wird.
</p>
</div>
<h3>Rechtliche Rahmenbedingungen</h3>
<div>
<p>Eine Ver&ouml;ffentlichung in openHSU setzt die dauerhafte &Uuml;bertragung des einfachen Rechts auf &ouml;ffentliche Zug&auml;nglichmachung des Dokuments im Internet durch den Urheber an die Universit&auml;tsbibliothek voraus. Dar&uuml;ber hinaus steht es dem Verfasser uneingeschr&auml;nkt frei, das vollst&auml;ndige Werk vor oder nach dem Erscheinen gegebenenfalls ver&auml;ndert oder in Ausz&uuml;gen anderweitig gedruckt oder elektronisch zu ver&ouml;ffentlichen.
</p>
</br>
<p>
<span class="highlight">Der Autor r&auml;umt der Universit&auml;tsbibliothek des Publikationsservers folgende Nutzungsrechte ein:</span>
</p>
<p>
<ul>
<li>das Recht zur elektronischen Speicherung,</li>
<li>das Recht zur Konvertierung zum Zwecke der Langzeitarchivierung bzw. Visualisierung, unter Beachtung der Bewahrung des Inhalts (die Originalarchivierung bleibt erhalten) und im Falle der Ver&ouml;ffentlichung,</li>
<li>das einfache Nutzungsrecht zur &ouml;ffentlichen Zug&auml;nglichmachung in internationalen Datennetzen gem&auml;&szlig; &sect; 19a UrhG,</li>
<li>das Recht zur Meldung sowie &Uuml;bergabe im Rahmen nationaler Sammelauftr&auml;ge oder an eine Langzeitarchivierungsstelle,</li>
<li>das Recht, die Metadaten, insbesondere Abstract und Inhaltsverzeichnis, unbeschr&auml;nkt jedermann zug&auml;nglich und nutzbar zu machen.</li>
</ul>
</p>
<p>
Die Universit&auml;tsbibliothek verpflichtet sich, die Autoren bei ihren Publikationsvorhaben zu unterst&uuml;tzen, die Dokumente zu archivieren und unter vertretbarem Aufwand dauerhaft verf&uuml;gbar zu halten. F&uuml;r den Inhalt der Dokumente sind ausschlie&szlig;lich die Autoren und Herausgeber verantwortlich. Die Universit&auml;tsbibliothek &uuml;bernimmt keine Haftung f&uuml;r die Inhalte der bereitgestellten Publikationen und verlinkter externer Seiten. Den Autoren bzw. dem Herausgeber der Publikation obliegt die Pflicht, eventuell betroffene Urheber- und Verwertungsrechte Dritter (z.B. Co-Autoren, Verlage, Drittmittelgeber) zu kl&auml;ren bzw. deren Einverst&auml;ndnis einzuholen. Erh&auml;lt der Autor Kenntnis vom Bestehen oder der Entstehung von Rechtshindernissen, setzt er die Universit&auml;tsbibliothek unverz&uuml;glich davon in Kenntnis. Die Universit&auml;tsbibliothek haftet nicht f&uuml;r aus der Verletzung von Urheber- und Verwertungsrechten resultierenden Sch&auml;den.
</p>
<p>Die Urheberrechte der Autoren bleiben gewahrt. Die Universit&auml;tsbibliothek empfiehlt die Verwendung von Creative-Commons-Lizenzen (kurz: CC-Lizenz). Durch Vergabe einer CC-Lizenz k&ouml;nnen bestimmte Nutzungsrechte an die Allgemeinheit &uuml;bertragen werden. Die Ver&ouml;ffentlichung auf openHSU steht einer weiteren Ver&ouml;ffentlichung der Dokumente in Fachzeitschriften oder Monographien sowie auf anderen Servern nicht entgegen. Die Metadaten der auf dem Publikationsserver ver&ouml;ffentlichten Dokumente d&uuml;rfen von jedermann &uuml;ber die OAI-Schnittstelle abgerufen, gespeichert und - gegebenenfalls in angereicherter Form oder in Auswahl - Dritten verf&uuml;gbar gemacht werden.
</p>
<p>
Bereits publizierte Dokumente werden nicht vom Server gel&ouml;scht. Bei Zweitpublikationen und Preprints kann in Ausnahmef&auml;llen bei Vorliegen eines wichtigen rechtlichen Grunds der Online-Zugriff eingeschr&auml;nkt werden. Preprints k&ouml;nnen auch aus inhaltlichen Gr&uuml;nden von den Autoren oder den Herausgebern zur&uuml;ckgezogen werden. Die Dokumente verbleiben jedoch im Archiv.
</p>
</div>

<h3>Archivierung von Dokumenten</h3>
<div>
<p>
<span class="highlight">Folgende Kategorien elektronischer Dokumente werden gespeichert und &uuml;ber openHSU verbreitet:</span>
</p>
<p>
<ul>
<li>Publikationen von Angeh&ouml;rigen der HSU mit wissenschaftlichem Inhalt wie Zeitschriftenaufs&auml;tze (auch Pre- und Postprints), Monographien, Teile aus Monographien, &Ouml;ffentliche Vorlesungen, Forschungsberichte, Studien, Schriften,</li>
<li>Dissertationen/ Habilitationen von Angeh&ouml;rigen der HSU oder durch Angeh&ouml;rige der HSU betreute</li>
<li>ausgew&auml;hlte Dokumente von Studierenden wie Diplom- und Magisterarbeiten, Bachelor- und Masterarbeiten, auf Empfehlung des Betreuers</li>
<ul>
</div>

<h3>Persistente Identifier</h3>
<div>
<p>
<span class="highlight">Der persistente Identifier DOI f&uuml;r Ihre Open Access-Ver&ouml;ffentlichungen:</span>
</p>
<p>
DOI steht f&uuml;r Digital Object Identifier und ist ein persistenter Identifier f&uuml;r den Nachweis digitaler Objekte/ Ressourcen. Die Vergabe eines DOI erm&ouml;glicht, dass eine elektronische Ressource einen eindeutigen, dauerhaften Nachweis erh&auml;lt und somit langfristig identifizierbar und zitierbar ist. Die Universit&auml;tsbibliothek erm&ouml;glicht die Vergabe eines DOI f&uuml;r Ihre Open-Access-Ver&ouml;ffentlichungen (dies gilt f&uuml;r Erst- und Zweitver&ouml;ffentlichungen). im Ver&ouml;ffentlichungsformular erfolgt die Genese der DOI in K&uuml;rze automatisch im Schritt "Persistent Identifier". Wir nutzen den <a href="https://www.tib.eu/de/publizieren-archivieren/doi-service/">DOI-Service der TIB Hannover</a>.
</p>
</div>

<h3>Organisatorische Infrastruktur</h3>
<div>
<p>
Das Forschungsinformationssystem openHSU der Helmut-Schmidt-Universit&auml;t/ Universit&auml;t der Bundeswehr Hamburg wird von der Universit&auml;tsbibliothek betreut. Die Ver&ouml;ffentlichung auf openHSU ist f&uuml;r Angeh&ouml;rige der HSU kostenfrei. Notwendige Unterst&uuml;tzung bei der Ver&ouml;ffentlichung bzw. bei (Vor-)Arbeiten k&ouml;nnen auf Wunsch von Mitarbeiter/innen der Universit&auml;tsbibliothek nach Absprache erfolgen.
</p>
<p class="highlight">
Wir freuen uns, Sie bei Ihren Fragen zu unterst&uuml;tzen! Schreiben Sie uns einfach an fis[at]hsu-hh.de.
</p>
</div>

<!--
<h3>Technische Infrastruktur</h3>
<div>
<p>
Coming soon
</p>
</div>
-->

<!--
<h3>Schnittstellen</h3>
<div>
<p>
Coming soon
</p>
</div>
-->

<h3>Autorenidentifikation mit ORCID</h3>
<div>
<p>
Eine ausf&uuml;hrliche Dokumentation s&auml;mtlicher Funktionen (in K&uuml;rze verf&uuml;gbar) in openHSU stellen wir Ihnen im Tab "ORCID" zur Verf&uuml;gung.
</p>
</div>

</div>
</div>


<!-- Mein openHSU-Tab  -->
<div id="tabs-2">
<div id="accordion2">
<h3>Login / Registrierung</h3>
<div>
<img class="img-thumbnail img-responsive" src="/image/Login.png"/>
<p>
</br>
</p>
<p>
<a href="https://openhsu.ub.hsu-hh.de/mydspace">Login in "Mein openHSU"</a>
</p>
<p>Bitte w&auml;hlen Sie &quot;Sign on to: My openHSU&quot;. Sie werden auf die Login-Seite mit folgenden Optionen weitergeleitet:
<ul>
<li>Enter DSpace Username and Password: Bitte w&auml;hlen Sie diese Option, wenn Sie keine RZ-Kennung besitzen, um sich zu registrieren bzw. einzuloggen</li>
<li>Login via Shibboleth: Bitte w&auml;hlen Sie diese Option, wenn Sie eine RZ-Kennung besitzen. Sie k&ouml;nnen sich auf der n&auml;chsten Seite aussuchen, ob Sie sich mit Ihrer RZ-Kennung oder Bibliothekskennung einloggen m&ouml;chten.</li>
<ul>
</div>



<h3>Mein Dashboard in openHSU (erste Orientierung)</h3>
<div>
<p>
Nach der Anmeldung in openHSU bekommen Sie Ihr Dashboard pr&auml;sentiert, welches alle Ihnen zur Verf&uuml;gung stehenden Handlungsoptionen &uuml;bersichtlich zusammenfasst:
</p>
<p>
<img class="img-thumbnail img-responsive" src="/image/Dashboard.png"/>
</p>
<p>
<a href="https://openhsu.ub.hsu-hh.de/mydspace">Mein Dashboard</a>
</p>
<p>
<ul>
<li><b>Wenn Sie sich zum ersten Mal einloggen, m&uuml;ssen Sie zun&auml;chst Ihren Account mit dem von uns f&uuml;r Sie vorbereiteten Forscherprofil matchen (wenn Sie ForscherIn an der HSU sind).</b></li>
<li>Nach dem Matching k&ouml;nnen Sie via &quot;View your profile&quot; zu Ihrem Profil navigieren, wo Sie u.a. auch die Einstellung vornehmen k&ouml;nnen, ob Ihr Profil &ouml;ffentlich oder privat sein soll.</li>

<li>Der Button &quot;Start a New Submission&quot; f&uuml;hrt Sie zum Ver&ouml;ffentlichungsformular, welches Sie bei der Eingabe aller ben&ouml;tigten Metadaten unterst&uuml;tzt.</li>

<li>Beim Klicken auf den Button &quot;View Accepted Submission&quot; bekommen Sie eine Liste aller Publikationen angezeigt, die Sie bis jetzt auf openHSU ver&ouml;ffentlicht haben.</li>

<li>Falls Sie bereits eine oder mehrere Ver&ouml;ffentlichungen gestartet, aber noch nicht fertig bearbeitet haben, werden Ihnen diese im unteren Bereich des Dashboards angezeigt (im Bereich &quot;View unfinished submissions&quot;). Hier finden Sie auch die Buttons zum &Ouml;ffnen des Formulars zur Weiterbearbeitung (Button &quot;Open&quot;) bzw. zum L&ouml;schen des vorhandenen Entwurfs (Button &quot;Remove&quot;). Eingereichte Ver&ouml;ffentlichungen, die sich aktuell im Freischaltungs-Workflow durch unsere Kollektions-Administratoren befinden, sehen Sie ebenfalls in diesem Bereich.</li>
</ul>
</p>
</div>


<h3>Alerting-Dienste</h3>
<div>
<p>
Coming soon
</p>
</div>


</div>
</div>



<!-- Mein Profil-Tab  -->
<div id="tabs-3">
<div id="accordion3">

<h3>Workflow (wie entsteht mein Profil)</h3>
<div class="container-fluid">
<p>Wie entsteht ein Forscherprofil?</p>
  <div class="col-sm-6">
   <div class="events">
     <div class="figcaption">
<p><span class="highlight">Die UB erstellt von jeder/m neuen MitarbeiterIn automatisch ein Profil. Dieses enth&auml;lt:</span></p>
<ul>
<li>Full Name,</li>
<li>Main Affiliation,</li>
<li>ggf. Titel,</li>
<li>Startdatum der Besch&auml;ftigung,</li>
<li>Art der Besch&auml;ftigung (Lehrstuhlinhaber, Wissenschaftliches Personal etc.),</li>
<li>E-Mail-Adresse (nicht &ouml;ffentlich sichtbar)</li>
</ul>
     </div>
   </div>
  </div>
  <div class="col-sm-6">
   <div class="events">
    <div class="figcaption">
<p><span class="highlight">Bei Lehrstuhlinhabern werden zus&auml;tzlich nachfolgende Informationen recherchiert und eingetragen:</span>
<ul>
<li>Researcher-ID,</li>
<li>ORCID,</li>
<li>Scopus-ID,</li>
<li>ResearchGate,</li>
<li>GoogleScholar,</li>
<li>Mendeley</li>
</ul>
</p>
  </div>
   </div>
  </div>
</div>


<h3>Profilpflege (was kann ich in meinem Profil bearbeiten)</h3>
<div>
<p>
Nach einer erfolgreichen Anmeldung haben Sie die M&ouml;glichkeit, Ihr pers&ouml;nliches Profil mit weiteren Informationen zu erg&auml;nzen oder zu &auml;ndern. 
Daf&uuml;r stehen Ihnen zum Beispiel im Tab "Profile" folgende Bereiche zur Verf&uuml;gung: 
<ul>
<li>Name Card,</li>
<li>General Information,</li>
<li>Education, Professional, Paedagogical, Scientific Qualification,</li>
<li>Participation in Organizations, Committees, Honours, Awards, Prizes</li>
</ul>
</p>
<hr>
<p>
<p><span class="highlight">&quot;Edit View&quot; eines Forscherprofils</span></p>
<img class="img-thumbnail img-responsive" src="/image/Profilpflege.png"/>
<hr>
<p><span class="highlight">&quot;Public View&quot; eines Forscherprofils</span></p>
<img class="img-thumbnail img-responsive" src="/image/Profil_II.png"/>
</p>

</div>


<h3>Sichtbarkeit der Profileintr&auml;ge (wer sieht was)</h3>
<div>
<p>
Als BesitzerIn Ihres Profils entscheiden Sie selbst, was von Ihren Daten f&uuml;r wen sichtbar sein sollte. Dazu haben Sie folgende Einstellungsm&ouml;glichkeiten:
</p>
<p>
<ul>
<li>Einstellung &quot;privat/&ouml;ffentlich&quot; f&uuml;r das gesamte Profil: die auf &quot;privat&quot; gestellten Profile sind in Ihrer Gesamtheit nur f&uuml;r Sie selbst und das Administratorenteam von openHSU sichtbar. Bei den Profilen, die auf &quot;&ouml;ffentlich&quot; gestellt sind, k&ouml;nnen Sie f&uuml;r jedes Attribut entsprechende Einstellungen vornehmen.</li>
<li>In der Bearbeitungsansicht (&quot;Edit Researcher Page&quot;) Ihres Profils k&ouml;nnen Sie bei jedem Attribut (Eintragungsfeld) durch das An- oder Ausklicken des K&auml;stchens rechts bestimmen, ob Ihre Daten &ouml;ffentlich sichtbar sein sollen (&uuml;ber dem Formular sehen Sie auch das entsprechende Hinweis &quot;Use the checkbox to hide values from public view&quot;.</li>
<li>F&uuml;r Administratoren sind alle Ihre Eintr&auml;ge sichtbar.</li>
</div>


<h3>Editierbarkeit der Profileintr&auml;ge (was kann ich bearbeiten)</h3>
<div>
<p>
Es gibt nur wenige Attribute, die aus Gr&uuml;nden der Datenkonsistenz NICHT durch Forschende ver&auml;ndert werden k&ouml;nnen. Diese Attribute fungieren als Schl&uuml;ssel oder Verbindungselemente zwischen verschiedenen Entit&auml;ten (Klassen) wie z.B. zwischen einer/m Forschenden und ihren/seinen Publikationen oder Organisationseinheit. Im seltenen Fall der notwendigen Ver&auml;nderung dieser Attribute wenden Sie sich bitte an das FIS-Team (fis@hsu-hh.de). Die unver&auml;nderbaren Attribute innerhalb eines Forscherprofils sind:
</p>



<!-- responsive Tabelle; 23.07.2020 --> 
<div class="g2g-respo-table">
<table>
<thead>
<tr>
<th>Attribut</th>
<th>Details</th>
</tr>
</thead>
<tbody>
<tr>
	
	<td data-label="Attribut"><font style="border-left: 4px solid #CFE107;">Full Name</font></td>
<td data-label="Details"
<font style="padding-bottom:10px;">(Voller Name): in der Regel in der Schreibweise, wie es in einem amtlichen Ausweis angegeben ist. Dieses Attribut wird vom Rechenzentrum bei der Anstellung angelegt und f&uuml;r openHSU aus dem RZ-Verzeichnis &uuml;bernommen.</font></td>
</tr>


<tr>
<td data-label="Attribut"></td>
<td data-label="Details"></td>
</tr>




<tr>
<td data-label="Attribut"><font style="border-left: 4px solid #CFE107;">Main Affiliation</td>
<td data-label="Details">(OrgUnit, &uuml;bergeordnete Organisationseinheit): diese wird oder werden aus dem Kostenstellenverzeichnis der Universit&auml;tsverwaltung &uuml;bernommen, um Abweichungen in der Schreibweise zu vermeiden.</td>
</tr>


<tr>
<td data-label="Attribut"></td>
<td data-label="Details"></td>
</tr>



<tr>
	<td data-label="Attribut"><font style="border-left: 4px solid #CFE107"; "padding-left:10px;">HSU E-Mail-Adresse</font></td>
<td data-label="Details">diese dient der Zuordnung von Forschenden zur Gruppe der Universit&auml;tsangeh&ouml;rigen. Es wird immer die HSU-E-Mailadresse eingetragen.</td>
</tr>
<tr>
<td data-label="Attribut"><font style="border-left: 4px solid #CFE107;">Status</td>
<td data-label="Details">es gibt nur drei Varianten: HSU Staff, Former HSU Staff, External Researcher. Der Status wird in &Uuml;bereinstimmung mit den Verwaltungsdaten von den Administratoren gesetzt.
</td>
</tr>



<tr>
<td data-label="Attribut"></td>
<td data-label="Details"></td>
</tr>



<tr>
<td data-label="Attribut"><font style="border-left: 4px solid #CFE107;">Rolle</td>
<td data-label="Details">diese wird beim Anlegen des Profils vom Administratorenteam eingetragen und dient dazu, die akademischen Strukturen innerhalb der HSU abzubilden (z.B. wissenschaftliche MitarbeiterIn, DekanIn oder DirektorIn).</td>
</tr>


<tr>
<td data-label="Attribut"></td>
<td data-label="Details"></td>
</tr>



<tr>
<td data-label="Attribut"><font style="border-left: 4px solid #CFE107;">ORCID</td>
<td data-label="Details">Alle ORCID-Attribute, da diese aus dem ORCID-Profil &uuml;bernommen werden. Bei &Auml;nderungsbedarf sollte das ORCID-Profil entsprechend ver&auml;ndert werden.</td>
</tr>


<tr>
<td data-label="Attribut"></td>
<td data-label="Details"></td>
</tr>


</tbody>
</table>
</div>

</div>


<h3>Datenschutz-Richtlinien</h3>
<div>
<p>
Hier finden Sie in K&uuml;rze Details zu:
<ul>
<li>DATEV</li>
<li>Selbst eingetragen = Einwilligung in die Datenverarbeitung</li>
<li>Dateneinsicht: Link zum Antrag folgt</li>
<li>Was passiert mit meinen Daten, wenn ich die Uni verlasse?</li>
<li>Daten l&ouml;schen: Link zum Antrag folgt. In welchen F&auml;llen m&uuml;ssen die Daten gel&ouml;scht werden? In welchen F&auml;llen d&uuml;rfen Sie noch behalten werden?</li>
</ul>
</p>
</div>


<h3>ORCID</h3>
<div>
<p>
Welche Einstellungen Sie n K&uuml;rze in Ihrem Forscherprofil bzgl. ORCID vornehmen k&ouml;nnen, lesen Sie bitte im Bereich <a href="https://openhsu.ub.hsu-hh.de/static/openhsuservices.jsp#tabs-6" target="_blank">ORCID</a> 
</p>
</div>



</div>
</div>


<!-- -Tab  -->
<div id="tabs-4">
<div id="accordion4">
<h3>Universit&auml;tsbibliografie und Publikationen vor Ihrer Zeit an der HSU</h3>
<div class="jumbotron" style="background-color:#466675">
<p style="color:#EDEBE7">In openHSU finden Sie in der Sammlung &quot;Publications of the HSU Researchers&quot; bereits ausgew&auml;hlte Publikations-Titeldaten unserer Pilotpartner f&uuml;r die Jahre 2017 bis 2019. Wir beginnen nun mit der retrospektiven Aufnahme der gesamten <a href="https://ub.hsu-hh.de/universitaetsbibliografie/" style="color: #CFE107">Universit&auml;tsbibliografie</a> bis (zun&auml;chst) einschlie&szlig;lich 2006. 
</p>
<p style="color:#EDEBE7">Bitte beachten Sie, da&szlig; die retrospektiven Titeldaten in einem mehrstufigen Verfahren eingespielt und nach und nach mit Forscherprofilen, Organisationseinheiten, Projekten, Schriftenreihen und Konferenzen verkn&uuml;pft werden.
</p>
<p style="color:#EDEBE7">
Eine zentrale Komponente des Forschungsinformationssystems openHSU ist die  <a href="https://ub.hsu-hh.de/universitaetsbibliografie/" style="color: #CFE107">Universit&auml;tsbibliografie</a>, da erst mit ihr ein realit&auml;tsgetreues Abbild der gegenw&auml;rtigen und ehemaligen Forschungsaktivit&auml;ten an der HSU entsteht durch die Verkn&uuml;pfung mit Forscherprofilen, Organisationseinheiten, Projekten, Konferenzen und Schriftenreihen.
</p>
<p style="color:#EDEBE7">Sie k&ouml;nnen in openHSU Ihre Publikationsliste vollst&auml;ndig pflegen und damit zugleich einen wertvollen Beitrag zur Weiterentwicklung der Universit&auml;tsbibliografie leisten.
</p>
<p style="color:#EDEBE7">
Bitte nutzen Sie hierf&uuml;r die Sammlung <a href="https://openhsu.ub.hsu-hh.de/handle/10.24405/342" style="color: #CFE107">Publications of the HSU Researchers</a>.
</p>
</div>


<h3>Publikations-Metadaten vor Ihrer Zeit an der HSU</h3>
<div>
<p>F&uuml;r die Eintragung von Publikations-Metadaten vor Ihrer Zeit an der HSU steht Ihnen die Sammlung <a href="https://openhsu.ub.hsu-hh.de/handle/10.24405/4492">Publications of the HSU Researchers (before HSU)</a> zur Verf&uuml;gung.
</p>
<hr>
<p class="highlight">Mit diesen beiden Sammlungen k&ouml;nnen Sie in openHSU Ihre vollst&auml;ndige Publikationsliste abbilden und dies zugleich in der Sammlung <a href="https://openhsu.ub.hsu-hh.de/handle/10.24405/342">Publications of the HSU Researchers</a> mit Open Access-Erst- und Zweitver&ouml;ffentlichungen verbinden, die im Kontext Ihrer T&auml;tigkeit an der HSU entstanden sind.
</p>
<p class="highlight">
Die einzelnen Schritte zum Eintrag einer Publikation in Ihre Publikationsliste bzw. zum Open Access Publizieren einer Erst- und Zweitver&ouml;ffentlichung haben wir f&uuml;r Sie unter &quot;Publizieren&quot; im Bereich <a href="https://openhsu.ub.hsu-hh.de/static/openhsuservices.jsp#tabs-5" target="_blank" >&quot;Einstiegspunkte Publizieren&quot;</a> beschrieben.
</p>
</div>

<h3>Aufnahme in die Universit&auml;tsbibliografie</h3>
<div>
<p>
Die Universit&auml;tsbibliografie weist alle selbst&auml;ndigen und unselbst&auml;ndigen Publikationen von Hochschulangeh&ouml;rigen seit 1973 nach, die im Zusammenhang mit einer T&auml;tigkeit an der Helmut-Schmidt-Universit&auml;t entstanden sind. Sie ist seit 2006 ein Teil der Forschungsdatenbank der Universit&auml;t und wird j&auml;hrlich f&uuml;r die Evaluation herangezogen. Es werden ausschlie&szlig;lich ver&ouml;ffentlichte Publikationen aufgenommen, die einer breiten &Ouml;ffentlichkeit in gedruckter oder elektronischer Form zug&auml;nglich sind. Da die Bibliothek vollst&auml;ndig alle Publikationen verzeichnen m&ouml;chte, ist sie hierbei auf die Meldung der Verfasser angewiesen. Dabei k&ouml;nnen Publikationen auch r&uuml;ckwirkend aufgenommen werden.
</p>
</div>


<h3>Export der Publikationsliste</h3>
<div>
<p>
Ihre Publikationen k&ouml;nnen Sie <span class="highlight">in K&uuml;rze</span> im Bereich &quot;Publications&quot; Ihres Forscherprofils exportieren. Diese Formate stehen Ihnen zur Verf&uuml;gung:
<ul>
<li>Refman</li>
<li>EndNote</li>
<li>BibTex</li>
<li>RefWorks</li>
<li>Excel</li>
<li>CSV</li>
<li>PDF</li>
<li>Send via email</li>
</ul>
<p>
Bitte beachten Sie, dass das Mapping des openHSU-Metadatenschemas (Dublin Core) auf die Felder der Exportformate noch nicht abgeschlossen ist. Daher ist noch kein vollst&auml;ndiger Export s&auml;mtlicher Metadaten m&ouml;glich. Sobald das Mapping abgeschlossen ist, informieren wir Sie an dieser Stelle.
</p>
</div>


<h3>Add fulltext-Service</h3>
<div>
<p>
Wenn Sie Universit&auml;tsbibliografie-Eintr&auml;ge in openHSU hinterlegt haben, k&ouml;nnen Sie diese (entsprechend der Lizenzbestimmungen Ihres Vertrags Ihrer Erstver&ouml;ffentlichung) ggf. sp&auml;ter noch mit dem Volltext erg&auml;nzen. Daf&uuml;r stehen Ihnen verschiedene Einstiegspunkte in das Ver&ouml;ffentlichungsformular mit Upload-Funktion zur Verf&uuml;gung.  	</p>
<p>
<ul>
<li>Neben den Metadaten der Publikation befindet sich rechts ein "Add fulltext"-Button. Sobald Sie auf diesen klicken, wird automatisch gepr&uuml;ft, ob Sie einer der Verfasser sind. Wenn Sie noch nicht eingeloggt sind, werden Sie davor zum Login gebeten.</li>
<li>In Ihrem Forscherprofil sowie in Profilen von Organisationseinheiten, Projekten, Konferenzen und Schriftenreihen befindet sich neben jeder Publikation, die keinen Volltext enth&auml;lt, ein Zahnrad mit der Option &quot;Add fulltext&quot;.</li>
</ul>


</div>



<h3>Suche</h3>
<div>
<p>
Coming soon
</p>
</div>



<h3>Statistiken / Bibliometrie</h3>
<div>
<p>
Coming soon
</p>
</div>


</div>
</div>


<!--Publizieren-Tab  -->
<div id="tabs-5">
<div id="accordion5">

<h3>Einstiegspunkte Publizieren</h3>
<div>
<h4 class="highlight">Schritt 1 - Auswahl der Sammlung und optional Abfrage oder Import bibliographischer Daten </h4>
<p>
<span class="highlight">Nach dem Login w&auml;hlen Sie bitte die von Ihnen gew&uuml;nschte Sprache des Eingabeformulars (im Bereich &quot;my OpenHSU&quot; einfach auf &quot;Deutsch&quot; oder &quot;English&quot; klicken). &Uuml;ber den Button &quot;Start a New Submission&quot; gelangen Sie anschlie&szlig;end direkt zum Ver&ouml;ffentlichungsformular. Dieses Formular steht Ihnen sowohl f&uuml;r Ihre Open Access-Publikationen (Erst- und Zweitver&ouml;ffentlichungen) als auch f&uuml;r Eintr&auml;ge in Ihre Publikationsliste zur Verf&uuml;gung. Sie k&ouml;nnen zun&auml;chst w&auml;hlen zwischen diesen Optionen:</span>
<ul>
<li><span class="italic">Default mode Submission</span>: nach Auswahl der Sammlung, in der Sie publizieren m&ouml;chten, w&auml;hlen Sie den Button &quot;Manual Submission&quot;, um direkt das Formular aufzurufen f&uuml;r eine manuelle Eingabe der Metadaten.</li>
<li><span class="italic">Search for identifier</span>: Wenn Ihre Publikation einen persistenten Identifier wie eine DOI hat oder Ihre Publikation beispielsweise bereits in Ihrem ORCID-Profil eingetragen ist, k&ouml;nnen Sie alternativ diesen Identifier in die entsprechende Suchzeile eintragen. Damit &uuml;bernehmen Sie komfortabel einige mit Ihrer Publikation verkn&uuml;pften Metadaten aus anderen Datenbank-Systemen.</li>
<li><span class="italic">Upload a file</span>: als weitere Variante k&ouml;nnen Sie Dateien in unterschiedlichen bibliographischen Formaten hochladen, um die Metadaten Ihrer Publikation(en) automatisiert in das Formular zu &uuml;bernehmen. Hier k&ouml;nnen Sie w&auml;hlen zwischen folgenden Formaten:</li>
<ul>
<li>PubMed XML,</li>
<li>CrossRef XML,</li>
<li>arXiv XML,</li>
<li>CiNii XML,</li>
<li><span class="highlight">BibTex</span>,</li>
<li><span class="highlight">Research Information Systems (RIS)</span>,</li>
<li><span class="highlight">Research Information Systems (RIS) exported from WOS</span>,</li>
<li><span class="highlight">EndNote</span>,</li>
<li><span class="highlight">Comma Separated Values CSV)</span>,</li>
<li>Tab Separated Values (TSV)</li>
</ul>
</ul> 
<hr>
<h4 class="highlight">Schritt 2 - Ver&ouml;ffentlichungsformular</h4>
<h4 class="highlight">Seite 1 - Eintrag in Ihre Publikationsliste oder Open Access-Ver&ouml;ffentlichung</h4>
<div class="jumbotron" style="background-color:#466675">

  <p style="color:#EDEBE7">Zu Beginn des Formulars k&ouml;nnen Sie per dropdown-Men&uuml; w&auml;hlen, ob Sie einen bibliografischen Eintrag in Ihre <font color="CFE107">Publikationsliste</font> vornehmen oder <font color="#CFE107">Open Access</font> publizieren m&ouml;chten.
</p>
<p style="color:#EDEBE7"> Diese Auswahl steuert nach den ersten drei Seiten des Formulars, ob ein Dateiupload-Schritt folgt oder nicht.
</p>
</div>
</br>

<img class="img-thumbnail img-responsive" src="/image/with_without_metadata.png"/>
<p>
</br>
</p>
<p> Im Ver&ouml;ffentlichungsformular sehen Sie auf der ersten Seite des Formulars, dass bei Auswahl des Modus "Search for identifier" oder "Upload a file" bereits einige Metadaten automatisch in das Formular &uuml;bertragen wurden. Bitte erg&auml;nzen Sie auf den Seiten 1 bis 3
fehlende Metadaten. Wenn Sie den &quot;Manual Submission Modus&quot; gew&auml;hlt haben, tragen Sie bitte s&auml;mtliche Metadaten ein, die f&uuml;r Ihre Publikation relevant sind. 
</p>
</br>

<h4 class="highlight">Seite 1 bis 3 - Verkn&uuml;pfungen zu Forscherprofilen, Organisationseinheiten, Projekten, Schriftenreihen und Konferenzen</h4>
<p>
Auf den ersten drei Seiten des Formulars k&ouml;nnen Sie die Metadaten Ihrer Publikation eingeben. Zudem k&ouml;nnen Sie in openHSU Verkn&uuml;pfungen zu Forscherprofilen, Organisationseinheiten, Projekten, Schriftenreihen oder Zeitschriften und Konferenzen erzeugen. Ihre Publikation ist dann mit dem Profil des Forschers, Projekts etc. verkn&uuml;pft.
</p>  
<div style="border-left: 4px solid #CFE107; padding-left: 10px;">
<p class="highlight">Forscherprofile</p>
</div>
<p>
Auf Seite 1 k&ouml;nnen Sie nach Eingabe des Autorennamens bzw. der Autorennamen auf die Lupe klicken, um eine Abfrage in der openHSU-Datenbank sowie im ORCID-Register zu starten.
Sofern sich der Autor bzw. die Autorin bereits in der Datenbank befinden, werden Ihnen in einem pop-up-Fenster die gefundenen Eintr&auml;ge offeriert (hinter dem Namen steht die entsprechende ID, z.B. Mustermann, Max (rp01909)).
Den Eintrag, den Sie gerne mit Ihrer Publikation verkn&uuml;pfen w&uuml;rden, &uuml;bernehmen Sie mit &quot;Accept&quot; in das Formular. Wenn sich der Autor noch nicht in der openHSU-Datenbank oder im ORCID-Register befindet, &uuml;bernehmen Sie bitte den Eintrag &quot;Local value 'Mustermann, Max' (not in Naming Authority)&quot;.
In diesem Fall wird erst im Zuge der Freischaltung Ihrer Publikation ein entsprechendes Forscherprofil erzeugt.
</p>
<div style="border-left: 4px solid #CFE107; padding-left: 10px;">
<p class="highlight">Organisationseinheit / Working Group / Sponsor oder F&ouml;rderer 
</p>
</div>
Auf der ersten Seite k&ouml;nnen Sie Ihre Publikation mit einer oder mehrerer
Organisationseinheiten verkn&uuml;pfen, an denen Ihre Arbeit entstanden ist. Wenn Sie den Namen eingeben, erhalten Sie ggf. Vorschl&auml;ge aus der openHSU-Datenbank, die Sie &uuml;bernehmen k&ouml;nnen, z.B. bei Eingabe von &quot;Auto&quot; &quot;Automatisierungstechnik (ou00180)&quot;.
</p>
<div style="border-left: 4px solid #CFE107; padding-left: 10px;">
<p class="highlight">Konferenzen
</p>
</div>
Auf Seite 3 des Ver&ouml;ffentlichungsformulars k&ouml;nnen Sie Ihre Publikation mit einer Konferenz verkn&uuml;pfen. Wenn die Konferenz bereits in der openHSU-Datenbank ist, k&ouml;nnen Sie diesen Eintrag &uuml;bernehmen.
</p>
<div style="border-left: 4px solid #CFE107; padding-left: 10px;">
<p class="highlight">Projekte
</p>
</div>
Auf Seite 3 des Ver&ouml;ffentlichungsformulars k&ouml;nnen Sie Verkn&uuml;pfungen zu Projekten erstellen. Wenn Ihr Projekt bereits in der openHSU-Datenbank hinterlegt ist, k&ouml;nnen Sie diesen Eintrag &uuml;bernehmen.
</p>
<div style="border-left: 4px solid #CFE107; padding-left: 10px;">
<p class="highlight">Schriftenreihen oder Zeitschriften
</p>
</div>
Auf Seite 3 befindet sich auch die Option, Ihre Publikation mit einer Schriftenreihe oder Zeitschrift zu verkn&uuml;pfen. Wenn Sie den Namen der Schriftenreihe oder Zeitschrift eingegeben haben, klicken Sie bitte auf das Lupen-Symbol und &uuml;bernehmen den Eintrag aus der openHSU-Datenbank, sofern sich die Schriftenreihe oder Zeitschrift bereits in der Datenbank befindet.
</p>
<hr>
<h4 class="highlight">Upload-Schritt (nur f&uuml;r Open Access-Publikationen)</h4>
<p>
F&uuml;r Open Access-Publikationen folgt nach den ersten drei Seiten des Ver&ouml;fentlichungsformulars der Upload-Schritt, in dem Sie die zu Ihrer Open Access-Publikation zugeh&ouml;rige(n) Datei(en) hochladen k&ouml;nnen. Bitte laden Sie PDF-Dokumente im Format PDF/A hoch. Weitere unterst&uuml;tzte Dateiformate k&ouml;nnen Sie auf der <a href="https://openhsu.ub.hsu-hh.de/help/formats.jsp">DSpace Supported Formats-Seite einsehen</a>.
</p>
<hr>
<h4 class="highlight">Verify-Schritt</h4>
<p>
Im n&auml;chsten Schritt k&ouml;nnen Sie Ihre Daten noch einmal pr&uuml;fen und ggf. korrigieren. Anschlie&szlig;end klicken Sie bitte auf &quot;Complete&quot;, um Ihren Publikationslisten-Eintrag bzw. Ihre Open Access-Publikation zu &uuml;bermitteln.
</p>
<hr>

<h4 class="highlight">Ver&ouml;ffentlichungsvertrag einreichen</h4>
<div class="jumbotron" style="background-color:#466675">
<p style="color:#EDEBE7">
Wenn Sie in openHSU Open Access publizieren m&ouml;chten, reichen Sie anschlie&szlig;end bitte den entsprechenden Ver&ouml;ffentlichungsvertrag ein. Die Vertr&auml;ge stehen f&uuml;r Sie zum Download auf der Seite <a href="https://ub.hsu-hh.de/publizieren/" style="color:#CFE107">&quot;Publizieren&quot;</a> bereit. 
</p>
</div>
</br>
<p>
Nach Vorlage des Vertrages erh&auml;lt Ihre Publikation einen DOI, um dauerhaft referenzierbar zu sein (weitere Informationen haben wir im Tab &quot;openHSU Leitlinien&quot; (&quot;Persistente Identifier&quot;) f&uuml;r Sie bereitgestellt. Danach wird Ihr Dokument durch die Universit&auml;tsbibliothek freigeschaltet. Abschlie&szlig;end erfolgt die Meldung Ihrer elektronischen Publikation an die Deutsche Nationalbibliothek. Dort wird sie mit den zugeh&ouml;rigen Metadaten registriert und langzeitarchiviert.
</p>

</div>

<h3>Erstver&ouml;ffentlichung</h3>
<div>
<p>
In openHSU k&ouml;nnen Sie folgende Publikationstypen als Erstver&ouml;ffentlichung ver&ouml;ffentlichen:
</p>
<ul>
<li>Dissertationen,</li>
<li>Wissenschaftliche Publikationen,</li>
<li>Studentische Abschlussarbeiten</li>
<li>Weiteres</li>
</ul>
</p>
</div>


<h4><span class="glyphicon glyphicon-arrow-right"></span> Publikationsfonds der HSU</h4>
<div class="jumbotron" style="background-color:#466675">
<p style="color:#EDEBE7">
F&uuml;r Ihre Open Access-Erstver&ouml;ffentlichungen steht Ihnen ein Publikationsfonds zur Verf&uuml;gung. Alle Details zur Antragstellung haben wir f&uuml;r Sie auf der <a href="https://ub.hsu-hh.de/open-access-publikationsfonds-der-hsu-unibw-h/" style="color:#CFE107">Website der Bibliothek</a> zusammengestellt.
</p>
</div>




<h3>Zweitver&ouml;ffentlichung</h3>
<div>
<p>
Nachfolgend haben wir einige wichtige Informationen f&uuml;r Sie zusammengestellt rund um das Thema Zweitver&ouml;ffentlichung.
</p>
</div>



<h4><span class="glyphicon glyphicon-arrow-right"></span>  Rechtliche Grundlagen</h4>
<div>
<p>
Mit dem seit dem 01.01.2014 geltenden Zweitver&ouml;ffentlichungsrecht erhalten Autoren und Autorinnen von wissenschaftlichen Beitr&auml;gen das einfache Nutzungsrecht, eine elektronische Manuskriptversion ihrer Publikation nach Ablauf eines Jahres seit der Erstver&ouml;ffentlichung &ouml;ffentlich zug&auml;nglich zu machen:
</p>
<p>&quot;Der Urheber eines wissenschaftlichen Beitrags, der im Rahmen einer mindestens zur H&auml;lfte mit &ouml;ffentlichen Mitteln gef&ouml;rderten Forschungst&auml;tigkeit entstanden und in einer periodisch mindestens zweimal j&auml;hrlich erscheinenden Sammlung erschienen ist, hat auch dann, wenn er dem Verleger oder Herausgeber ein ausschlie&szlig;liches Nutzungsrecht einger&auml;umt hat, das Recht, den Beitrag nach Ablauf von zw&ouml;lf Monaten seit der Erstver&ouml;ffentlichung in der akzeptierten Manuskriptversion &ouml;ffentlich zug&auml;nglich zu machen, soweit dies keinem gewerblichen Zweck dient. Die Quelle der Erstver&ouml;ffentlichung ist anzugeben. Eine zum Nachteil des Urhebers abweichende Vereinbarung ist unwirksam.&quot; (&sect; 38 Abs. 4 UrhG)
</p>
</br>
<p>
<span class="highlight">F&uuml;r die Zweitver&ouml;ffentlichung m&uuml;ssen also folgende Bedingungen erf&uuml;llt sein:</span>
<ul>
<li>Es handelt sich um einen wissenschaftlichen Beitrag</li>
<li>Der Beitrag ist aus einer mindestens zur H&auml;lfte mit &ouml;ffentlichen Mitteln gef&ouml;rderten Forschungst&auml;tigkeit entstanden</li>
<li>Der Beitrag ist in einer periodisch mindestens zweimal j&auml;hrlich erscheinenden Sammlung erschienen</li>
<li>Die Embargofrist von 12 Monaten ist einzuhalten, es sei denn, mit dem Verlag der Erstver&ouml;ffentlichung wurde eine k&uuml;rzere Frist vereinbart.</li>
</br>
</ul>
<p><span class="highlight">Au&szlig;erdem sollten folgende Punkte beachtet werden:</span>
<ul>
<li>Eine Einreichung z.B. bei einem Repositorium vor Ablauf der Embargofrist ist erlaubt, wenn die Freischaltung erst nach der Embargofrist erfolgt.</li>
<li>Das ZVR kann nur einmalig pro Publikation genutzt werden: d.h. die AutorInnen k&ouml;nnen das Manuskript an nur einem Ort &ouml;ffentlich zug&auml;nglich machen</li>
<li>Bei mehreren AutorInnen steht das ZVR allen KoautorInnen zu. Will ein/e AutorIn der Gruppe das Zweitver&ouml;ffentlichungsrecht nutzen, ben&ouml;tigt er oder sie die Zustimmung aller anderen AutorInnen des Werkes</li>
<li>Das Zweitver&ouml;ffentlichungsrecht sieht eine Pflicht zur Nennung der Originalpublikation vor.</li>
<li>Die Zweitver&ouml;ffentlichung darf nicht zu gewerblichen Zwecken genutzt werden.</li>
</ul>

</div>


<h4><span class="glyphicon glyphicon-arrow-right"></span>  Green-Open-Access-Services der Bibliothek</h4>
<div>
<p>
<span class="highlight">Die Universit&auml;tsbibliothek der HSU m&ouml;chte ihren tatkr&auml;ftigen Beitrag zur fl&auml;chendeckenden Abbildung der an der HSU betriebenen Forschung leisten, indem sie folgende Dienstleistungen im Bereich der Zweitver&ouml;ffentlichung plant:</span>
</p>
<p>
<ul>
<li>&Uuml;berpr&uuml;fung der Publikation auf ihre Eignung zur Zweitver&ouml;ffentlichung anhand der gesetzlichen Kriterien</li>
<li>&Uuml;berpr&uuml;fung von m&ouml;glichen bereits existierenden Zweitver&ouml;ffentlichungen der Publikation (Websites der Organisation, Repositorien, private Websites usw.)</li>
<li>U.U. &Uuml;berpr&uuml;fung der Vertragseinzelheiten der Erstver&ouml;ffentlichung (z.B. eine k&uuml;rzere Embargofrist oder Regelungen zur Manuskriptverwendung und Quellenangabe)</li>
<li>&Uuml;berpr&uuml;fung einer m&ouml;glichen Geltung von Allianz-/Nationallizenzvereinbarungen, falls das Zweitver&ouml;ffentlichungsrecht bei einem bestimmten Beitrag nicht greifen sollte</li>
<li>&Uuml;berpr&uuml;fung der Publikation auf ihre Eignung zur weiteren Ver&ouml;ffentlichung anhand der Open-Access-Policies des (Erstver&ouml;ffentlichungs-)Verlags</li>
<li>Unterst&uuml;tzung beim Einholen der Einverst&auml;ndnis von den KoautorInnen des Beitrags</li>
<li>Einspeisung der Metadaten der geeigneten Beitr&auml;ge in die Sammlungen in openHSU</li>
<li>Aufbereitung des Manuskripts und ggf. Upload in openHSU</li>
</ul>
</br>
<p>
<span class="highlight">Beachten Sie bitte, dass es sich bei diesen Services um keine juristische Beratung handelt.</span>
</p>
</div>


<h4><span class="glyphicon glyphicon-arrow-right"></span>  Werkzeuge</h4>
<div>
<p>
<ul>
<li>SherpaRoMeo (geplant)</li>
<li>Unpaywall-API (geplant)</li>
</ul>
</p>
</div>



<!--
<h3>Open Access Basics</h3>
<div>
<p>
Coming soon
</p>
</div>
-->

<!--
<h4><span class="glyphicon glyphicon-arrow-right"></span> Lizenzen</h4>
<div>
<p>
Im Ver&ouml;ffentlichungsformular k&ouml;nnen Sie auf Seite 2 eine Lizenz f&uuml;r Ihre Open Access-Publikation ausw&auml;hlen. Detaillierte Informationen zu den CC-Lizenzen finden Sie auf der <a href="https://creativecommons.org/licenses/?lang=de">/Creative Commons Website</a>.
</p>
</div>
-->



</div>
</div>





<!-- ORCID-Tab  -->
<div id="tabs-6">
<div id="accordion6">
<h3>ORCID</h3>
<div>
<div class="jumbotron" style="background-color:#466675">
<p style="color:#EDEBE7">
Die nachfolgend skizzierten <font color="#CFE107">ORCID-Funktionen</font> stehen Ihnen in K&uuml;rze zur Verf&uuml;gung.
</p>
</div>
</br>
<p>
ORCID steht f&uuml;r Open Researcher Contributor ID und dient in der Wissenschaft der eindeutigen Identifizierung eines Autors. Es handelt sich um einen alphanumerischen Code, welcher an eine Person dauerhaft vergeben wird. F&uuml;r die Erstellung Ihrer ORCID k&ouml;nnen Sie sich auf orcid.org registrieren.
Wenn Sie bereits &uuml;ber einen ORCID Zugang verf&uuml;gen, lassen sich openHSU und ORCID miteinander verkn&uuml;pfen. Ihre in ORCID eingetragenen Publikationen k&ouml;nnen Sie ganz bequem in openHSU &uuml;bertragen.
Auf <a href="orcid.org">ORCID.org</a> finden Sie ausf&uuml;hrliche Informationen zu ORCID.</p>
<hr>
<h4 class="highlight">Schritte in openHSU</h4>
<h4 class="highlight">Schritt 1 - ORCID-Profil erstellen (wenn Sie noch kein ORCID-Profil besitzen)</h4>
<p>
Wenn Sie sich noch nicht bei ORCID registriert haben, k&ouml;nnen Sie sich bequem von DSpace-CRIS aus in ORCID registrieren. In Ihrem Forscherprofil finden Sie im Tab "ORCID" den Bereich "ORCID Authorizations". Dort w&auml;hlen Sie bitte "Create an ORCID ID". Sie werden zu ORCID weitergeleitet und k&ouml;nnen dort den Reiter "Institutional account" nutzen, um sich mit Ihrer RZ-Kennung zu registrieren. Wichtig ist, dass Sie Ihre E-Mail-Adresse in Ihrem ORCID-Profil auf sichtbar stellen, damit Sie sp&auml;ter Ihr Profil in openHSU mit ORCID verkn&uuml;pfen k&ouml;nnen. Seitens ORCID wird zudem empfohlen, dass Sie neben Ihrer institutionellen E-Mail-Adresse noch eine zweite E-Mail-Adresse hinterlegen, um den Zugriff auf das ORCID-Profil im Falle eines institutionellen Wechsels zu erhalten und der Erstellung eines duplikaten ORCID-Profils vorzubeugen.
<hr>
<h4 class="highlight">Schritt 2 - openHSU mit ORCID-Profil verkn&uuml;pfen</h4>
<p>
Wenn Sie bereits ein ORCID-Profil besitzen (oder eben erstellt haben), k&ouml;nnen Sie Ihr Profil in openHSU mit ORCID verkn&uuml;pfen. Bitte w&auml;hlen Sie hierzu in der &quot;Public View&quot; Ihres Forscherprofils im Tab "ORCID" die Option "Connect to ORCID". Diese Verkn&uuml;pfung autorisiert openHSU, Ihre in openHSU eingetragenen Publikations-Metadaten, Projekte sowie biographischen Informationen mit Ihrem ORCID-Profil zu synchronisieren.
</p>
<hr>
<h4 class="highlight">Schritt 3 - Synchronisierungsmodus w&auml;hlen</h4>
<p>
In der "Edit View" Ihres Forscherprofils k&ouml;nnen Sie w&auml;hlen, ob sie den manuellen oder automatischen Synchronisierungsmodus pr&auml;ferieren. Diese Einstellung k&ouml;nnen Sie im Bereich "ORCID Synchronization settings" vornehmen und speichern. Wenn Sie dort keine Einstellung vornehmen, ist der manuelle Modus aktiviert.
</p>
</br>
<p class="highlight">
Welche Berechtigungen Sie openHSU f&uuml;r die Synchronisierung mit Ihrem ORCID-Profil gegeben haben, k&ouml;nnen Sie in der &quot;Public View&quot; Ihres Forscherprofils im Tab &quot;ORCID&quot; einsehen:
<ul>
<li>&quot;Get your ORCID iD&quot; (&quot;Will share your sixteen-character ORCID iD and any public information on your ORCID record&quot;)</li>
<li>&quot;Read your ORCID record&quot; (&quot;Will allow this organization to read information in your ORCID record which you have shared with trusted parties&quot;)</li>
<li>&quot;Update your biographical information | Update your works&quot; (&quot;Will allow this organization or application to make changes ONLY to works it has previously posted to your ORCID record&quot;)</li>
</ul>
</p>

<hr>

<h4 class="highlight">Schritt 4 - Synchronisierung von Publikationen, Projekten und biographischen Informationen definieren</h4>
<p class="highlight">F&uuml;r die Synchronisierung von Publikationen k&ouml;nnen Sie im Bereich "Publications Preferences" definieren, ob Sie 
<ul>
<li>keine Ihrer in openHSU eingetragenen Publikationen mit ORCID synchronisieren m&ouml;chten (&quot;Disable&quot;),</li>
<li>s&auml;mtliche Ihrer in openHSU eingetragenen Publikationen mit ORCID synchronisieren m&ouml;chten (&quot;Send all publications&quot;)</li>
<li>Ihre ausgew&auml;hlten Publikationen mit ORCID synchronisieren m&ouml;chten (&quot;Send my selected publications&quot;)</li>
<li>Ihre &ouml;ffentlich sichtbaren Publikationen mit ORCID synchronisieren m&ouml;chten (&quot;Send my publications&quot;)</li>
</ul>
</p>
</br>

<p class="highlight">F&uuml;r die Synchronisierung von Projekten k&ouml;nnen Sie im Bereich "Projects Preferences" definieren, ob Sie
<ul>
<li>keine Ihrer in openHSU eingetragenen Projekte mit ORCID synchronisieren m&ouml;chten (&quot;Will allow to not send projects&quot;),</li>
<li>s&auml;mtliche Ihrer in openHSU eingetragenen Projekte mit ORCID synchronisieren m&ouml;chten (&quot;Send all projects&quot;),</li>
<li>Ihre ausgew&auml;hlten Projekte mit ORCID synchronisieren m&ouml;chten (&quot;Will allow to send only selected projects&quot;),</li>
<li>Ihre &ouml;ffentlich sichtbaren Projekte mit ORCID synchronisieren m&ouml;chten (&quot;Will allow to send only projects not hidden&quot;)</li>
</ul>
</p>
</br>

<p class="highlight">F&uuml;r die Synchronisierung biographischer Informationen k&ouml;nnen Sie im Bereich "Profile Preferences" definieren, welche der folgenden Felder Sie mit Ihrem ORCID-Profil synchronisieren m&ouml;chten:
<ul>
<li>affiliations-employment (entspricht dem Feld &quot;Affiliations (HSU)&quot; in der &quot;Edit View&quot; Ihres Forscherprofils)</li>
<li>external-identifier-Researcher ID (entspricht dem Feld &quot;Researcher ID&quot;)</li>
<li>affiliations-education (entspricht dem Feld &quot;Education&quot;)</li>
<li>iso-3166-country (entspricht dem Feld &quot;Country&quot;)</li>
<li>keywords (entspricht dem Feld &quot;Keywords&quot;)</li>
<li>researcher-urls (entspricht dem Feld &quot;Personal Sites&quot;)</li>
<li>external-identifier-Scopus Author ID (entspricht dem Feld &quot;Scopus Author ID&quot;)</li>
<li>other-names (entspricht dem Feld &quot;Variants&quot;)</li>
</ul>
</p>
<hr>

<h4 class="highlight">Schritt 5 - relevant bei Auswahl des manuellen Synchronisierungsmodus in Schritt 3</h4>
<p>
Wenn Sie in Schritt 3 den manuellen Synchronisierungsmodus gew&auml;hlt haben, sehen Sie in der &quot;Public View&quot; Ihres Forscherprofils im Tab &quot;ORCID&quot;, welche Publikationen, Projekte und biographischen Informationen Sie in der &quot;ORCID Registry Queue&quot; mit Ihrem ORCID Profil aktuell synchronisieren k&ouml;nnen. 
</p>

</div>

</div>
</div>



<!--FAQ und News rund um das FIS-Tab  -->
<div id="tabs-7">
<div id="accordion7">

<!-- mehrere grid container mit Abstand nebeneinander; A.G., 23.07.2020 -->
<h3><span class="glyphicon glyphicon-tasks"></span>  openHSU Webinar-Reihe</h3>
<div class="jumbotron" style="background-color:#466675"; "display: inline-block"; "position: relative"; "width: 100%"; "background-size: contain">
  <div class="col-sm-6">
   <div class="events">
     <div class="figcaption">
     <p style="color:#EDEBE7">
Zur Zeit planen wir eine <font color="#CFE107">Webinar-Reihe</font> f&uuml;r alle Services in openHSU!</br>
</p>
     </div>
   </div>
  </div>
  <div class="col-sm-6">
   <div class="events">
    <div class="figcaption">
      <p style="color:#EDEBE7">
Senden Sie uns gerne Ihre Themenw&uuml;nsche an fis[at]hsu-hh.de.</br>
</p>
    </div>
   </div>
  </div>
   </div>




<h3><span class="glyphicon glyphicon-envelope"></span> Kontakt</h3>
<div>
<strong>Schreiben Sie uns gerne an fis[at]hsu-hh.de!</strong>
<p>
<ul>
<li>Fragen zum Open Access-Publizieren und CC-Lizenzen: openaccess[at]hsu-hh.de</li>
<li>Fragen zur Technik: dspace[at]hsu-hh.de</li>
<li>Fragen zur Ablieferung der gedruckten Fassung einer Dissertation: ediss[at]hsu-hh.de</li>
<li>Fragen bez&uuml;glich Promotionsordnungen: Die jeweils zust&auml;ndige Fakult&auml;tsverwaltung.</li>
<ul>	
<li><a href="https://www.hsu-hh.de/fkvet/">MB/ ET</a> | <a href="https://www.hsu-hh.de/geiso/fakultaetsverwaltung-edv">GeiSo</a> | <a href="https://www.hsu-hh.de/wiso/fakultaetsverwaltung">WiSo</a></li>
</ul>

</div>

</div>
</div>

<!-- Ende der Tabs und Akkordeons; A.G., 13.05.2018  -->

<!-- schlie?endes tag div container fullHeight  -->

<!-- verirrtes div-end-tag; A.G., 13.05.2018  -->
<!-- </div> -->
<!-- dito  -->
</div>




</div>
<!-- </main> -->


<!-- schlie?endes tag div class="col-sm-12"; A.G., 13.05.2018  -->
</div>
<!--</body>-->

<!-- schlie?endes tag div class="row"; A.G., 13.05.2018  -->
</div>



<!-- schlie?endes tag div class="container"; A.G., 13.05.2018  -->
</div>


<!-- schlie?endes tag div container="fullHeight"; A.G., 13.05.2018  -->
</div>

<!--</div>-->
</br>
</main>

<!-- Footer fuer Serviceseite mit php / jsp include eingefuegt; Abstand eingefuegt; A.G., 06.05.2018  -->
<!-- XAMPP-Version: php include -> footer_default_xampp.html; 
Hermes-Version: jsp include -> footer-default.jsp; A.G., 08.05.2018  -->
<div id="footer">

<!-- Einbindung des Footers; A.G., 11.05.2018  -->

	<jsp:include page="footer-default.jsp"/>	
	 
	 </div>		
	
		
 </body> 
 
 </div>
	
	

  
</html>

