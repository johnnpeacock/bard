%{-- Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 --}%

<!DOCTYPE html>
<html>
<head>
    <r:require
            modules="core,bootstrap,twitterBootstrapAffix"/>
    <meta name="layout" content="howto"/>
    <r:external file="css/bootstrap-plus.css"/>
    <title>BARD How to filter search results</title>

</head>

<body>
<div class="search-panel">
    <div class="container-fluid">
        <div class="row-fluid">
            <aside class="span2"></aside>
            <article class="span8 head-holder"><h2>How to Filter Search Results</h2></article>
            <aside class="span2"></aside>
        </div>

    </div>
</div>


<article class="hero-block">
<div class="container-fluid">
<div class="lowkey-hero-area">
<div class="row-fluid">
<aside class="span2"></aside>
<article class="span8">


    <h3>
        Overview
        </h3>
    <p>
        On the left side of the results page, BARD may provide you with a menu of filters you can apply to quickly refine your results within a particular tab. The types of filters that are available will vary depending upon your search results. If your search yields very few and/or similar results, it is possible that no filters will be displayed.
        </p>

    <p>
        As you refine your results, BARD’s query cart makes it easy to set aside the most interesting ones for further investigation. Each result has a checkbox that enables you to select the result to be added to your query cart for export and visualization. You can select results one at a time or you can “ADD ALL” the results under that tab with a single click. In the upper right corner of the page, the “QUERY CART” button shows the number of items in your cart. Click the button to view a list of those items.
        </p>

    <h3>
        Understanding Filters
        </h3>
    <p>
        Just as BARD generates results that are specific to each search, it also generates a unique set of filters that are specific to each search’s results. Sometimes you’ll have many filtering options, other times very few to none – it depends upon your search. These filters, when available, will appear along the left side of the results page so you can quickly and easily fine-tune the results under a particular tab.
        </p>

    <p>
        The filters are grouped by data category and/or other parameters directly relevant to the activated tab and its results. When you first arrive at the results page, or first activate a tab, you will see the filter groups listed by name. The number next to each group name is the number of search results that will remain if you apply all the filters in that group. You will also see a box with a “+” that will reveal the individual filters within that group.
        </p>

    <IMG style="margin: 25px auto 25px;" SRC="${resource(dir: 'images/bardHomepage', file: 'aspect_filters_screen_capture.png')}" ALIGN="top">

    <p>
    When you open a group to reveal its filters, you will see each filter listed by name and the number of results that will be displayed if that filter is applied. For example, if a filter in the assay definition tab says, “cell-based format (5),” activating that filter will narrow the results to 5 assay definitions that use a cell-based format.
    </p>

<aside class="calloutbox">
    <h3>
        When the Filter Group Total Doesn’t Agree With the Total of Individual Filters
        </h3>
<p>
    Don’t be confused by rare instances when the sum of results from all the individual filters in a group is higher than the number next to the group name. This discrepancy occurs because two filters within that group share the same result, but that shared result is only counted once in the group total.
    </p>

</aside>

    <h3>
        Working With Filters
        </h3>
    <p>
        Each filter has a checkbox to select it, but it is important to remember that the filter will not be activated until you click "APPLY FILTERS".
        </p>

    <p>
        You may select filters from multiple groups at the same time. This filtering strategy will quickly narrow results, perhaps even to zero, depending on whether or not results exist that simultaneously match all of the selected filters. By experimenting with different combinations you can reveal interesting connections between data that might not be obvious otherwise.
        </p>

    <p>
        When your search returns multiple pages of results, you can apply and clear filters from any page. However, you will be returned to the first page as the tab is repopulated with the newly filtered results. Keep in mind that a result that was on one page may move to another page.
        </p>

    <p>
        Filters are only applicable to their own tab. So if you apply filters to the results in the assay definitions tab, they will have no effect on the results in the compounds or projects tabs, or anywhere else in BARD. “CLEAR FILTERS” is also exclusive to the filters on that tab. However, the filters will be retained during your entire search session. So if you apply filters to the compounds tab, view a different tab and then return to the compounds tab, your original compound tab filters will still be applied.
        </p>


    <h3>
        Query Cart
        </h3>
    <p>
        As you perform searches and refine your results with filters, you can use the query cart to collect data of interest for further analysis, visualization and export. Your query cart is unique to you and the cart contents only persist during a single session. Your query cart will be cleared when you quit your web browser. Think of your query cart as a live work and storage space for you to temporarily gather data for visualization and export.
        </p>

    <p>
        You can add assay definitions, compounds and projects to your cart. Just click the "SAVE TO CART" checkbox for that item on either the search results page or the item’s detail page. You can also choose to “ADD ALL” results from a tab on the search results page.  This will add all the items currently displayed; to add additional items from other pages, you must navigate to those pages and “ADD ALL” again. To remove an item from your cart, just uncheck the box.
        </p>

    <p>
        In the upper right corner of the page, the “QUERY CART” button shows the number of items in your cart. Click the button to view a list of those items. You can then choose to “VISUALIZE” those results then select “Molecular Spreadsheet”. You can also open the contents of your cart in the BARD desktop client by selecting the "VIEW IN DESKTOP CLIENT" option instead.
        </p>

    <IMG style="margin: 25px auto 25px;" SRC="${resource(dir: 'images/bardHomepage', file: 'query_cart_screen_capture.png')}" ALIGN="top">

    <p>
    If you have only compounds in your cart, you will see the results for each of the experiments that the compounds tested active in. If you have compounds and projects or assay definitions in your cart, you will see the results for those compounds filtered to show only the given projects or assay definitions. Currently, a molecular spreadsheet cannot be generated from a cart that contains only assay definitions or projects (this function will be available in early 2014); you must have at least one compound.
    </p>

    <p>
        When looking at the list of items in your query cart, you can click the names to view the details page for each item. You can remove an item by clicking the “X” next to it, or you can empty the entire cart by clicking “CLEAR ALL.”
    </p>




</article>
<aside class="span2"></aside>
</div>
</div>
</div>
</article>



</body>
</html>
