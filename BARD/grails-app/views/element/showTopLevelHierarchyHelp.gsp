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

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <r:require modules="core,bootstrap"/>
    <meta name="layout" content="basic"/>
    <title>BARD Hierarchy Top-Level Concept Definitions</title>

</head>

<body>
<div class="row-fluid">
    <div class="offset3 span6">
        <h1>BARD hierarchy top level concept definitions</h1>

        <hr/>

        <h2>Overview</h2>

        <p>The definition of an assay (aka "assay definition") in BARD consists of two top-level concepts: the <strong>assay protocol</strong>, which defines the experimental conditions, and the <strong>biology</strong>, which defines the presumed biological subject of the assay, independent of the experimental conditions. Each of these concepts contains a set of terms that specify their details as the <strong>assay definition</strong>, which can be reused whenever depositing new data that were collected with all the same parameters defined in these two sets of terms.
        </p>

        <p>The third top-level concept, <strong>project management</strong>, captures the information that is newly specified each time an <strong>assay protocol</strong> is executed, defined as an <strong>experiment</strong> (conceptually similar to a 'run' or an 'assay instance'). A project (e.g., a screening campaign) is a <strong>collection of experiments run to identify, validate, and optimize probe or tool compounds to interrogate specific biology</strong> . Individual experiments may or may not be associated to a project, although in the MLP they typically are associated with a project.
        </p>

        <p>The fourth top-level concept, <strong>result</strong>, defines the types of measurements captured by the <strong>assay protocol</strong>, including both measured and derived values. This section also includes details such as parameters used in derivations, thresholds for "hit" calling and quality assessment parameters. Results are treated separately from the <strong>assay definition</strong> so that appending new result measurements to an existing <strong>assay definition</strong>, e.g., changing from single-point percent inhibition to IC50 curves, is allowed while maintaining the same <strong>assay protocol</strong> and <strong>biology target</strong>.
        </p>

        <p>Each <strong>experiment</strong> (a.k.a. run, assay instance) in BARD combines an <strong>assay definition</strong> (<strong>assay protocol</strong> + <strong>biology</strong> ) with one or more result types. <strong>Each experiment is further annotated by the structure of its results, how the measured values were captured and reported (such as tested concentrations), and whether the <strong>experiment</strong> was executed to support an existing project.
        </strong></p>

        <p>Each <strong>experiment</strong> may vary in certain parameters, usually related to the perturbagens being tested, that do not change the underlying <strong>assay protocol</strong> or <strong>biology</strong> (together, the <strong>assay definition</strong>). For example, the same <strong>assay definition</strong> may be run at a single concentration or in dose-response format, against a different compound collection, <strong>or by a different laboratory</strong> - the exact same <strong>assay protocol</strong> run by different groups will be considered the same <strong>assay definition</strong>. In addition, a single <strong>assay definition</strong> may be used for different purposes in different overall screening projects or campaigns, perhaps as a primary screen in one case and as a counterscreen in another. Finally, in addition to the role of an <strong>experiment</strong> in a screening campaign, the <strong>project management</strong> concept captures the overall purpose (e.g., therapeutic discovery for disease X) of the project with which each <strong>experiment</strong> is associated.
        </p>


        <h2>First-level term definitions from the BARD ontology</h2>

        <hr/>

        <p><em>Sentences in italics are taken directly from the RDM master files.</em></p>

        <dl>

            <dt>assay protocol</dt><dd>An experimental protocol designed to test the effect of a perturbagen on a biological entity, measuring one or more readouts facilitated by an assay design and assay type, and recording the results as one or more endpoints that quantify or qualify the extent of perturbation.</dd>

            <dt>biology</dt><dd>A biological entity or process that is the presumed subject of the assay; may refer to a macromolecule whose activity is being regulated, or to a cell-biological process (e.g., neurite outgrowth). Refers specifically to the assay being defined without regard to the larger project context in which the assay may be executed; the larger project biological context is captured separately, in the 'project management' section.</dd>

            <dt>result</dt><dd>A description of a set of data generated by executing an <strong>assay protocol</strong> and the context of those data, including defining what parameters the results are measuring and what calculation methods are used to derive the results. These parameters refer to the entire dataset, not to the specific results of any one perturbagen; the latter can be viewed individually only following data submission.
        </dd>

            <dt>project management</dt><dd>Information specific to the project under which a deposited <strong>experiment</strong> was run; does not modify the experimental conditions of the defined <strong>assay protocol</strong>, but provides a larger context for interpreting the results.
        </dd>

        </dl>

        <h2>Second-level terms</h2>

        <hr/>

        <p>Each of these top-level groups has a small number of subclasses that help provide context for what kinds of information are being captured in each group. The most important of these subsets is related to the <strong>assay protocol</strong> group; within the <strong>assay protocol</strong> subclasses, the most important defining feature is the <strong>assay format</strong>, which is used as a key to determine what other parameters are necessary to further define the <strong>assay protocol</strong>.
        </p>

        <p>Sentences in italics are taken directly from the RDM master files.</p>

        <h2>Assay protocol subclasses</h2>
        <dl>

            <dt>assay format</dt><dd><em>A concept defining an assay based on the biological or chemical features of the assay components, including biochemical assays with purified protein, cell-based assays performed on whole cells, and organism-based assays performed in an organism.</em>
        </dd>

            <dt>assay component</dt><dd><em>A physical entity participating in the assay reaction, common across all reactions in the assay (with the exception of a control that may be in a specified reactions), but not including the entity being queried for a measured effect on the assay (i.e., perturbagen); the purpose of each component is defined by 'assay component role'.</em>
        </dd>

            <dt>assay design</dt><dd><em>The physical parameters associated with executing and measuring the assay; defines how the assay components are used to generate data.</em>
        </dd>

            <dt>assay measurement</dt><dd><em>An abstract concept to group one or more assay readout and allow description of an assay that measures one or more effect of a perturbagen on the biological entity; does not define the physical detection readout itself, which is captured in assay design, but rather classifies the data being generated by readouts; e.g., single point vs. multiparametric.</em>
        </dd>

            <dt>assay type</dt><dd><em>A general description of what class of biological process is being measured by the assay and what technology is used to perform the measurement.</em>
        </dd>

        </dl>

        <h2>Subclasses of the other top-level groups</h2>

        <hr/>

        <p>biology has two subclasses, the specific molecular target (if it is known) and a biological process or molecular function term that is found in the Gene Ontology. Note the distinction between molecular target as defined under <strong>biology</strong> vs. <strong>assay component</strong> - the former refers to the abstract target (e.g., kinase X) while the latter refers to the specific physical entity used in the assay (e.g., kinase X residues 1-N, C-terminal His6 tag). It is acceptable to define multiple targets or both a molecular target and a biological process, if appropriate.
        </p>

        <dl>
            <dt>result -> result type</dt>
            <dd><em>A classification of different types of results that are reported for an <strong>experiment</strong>, providing the context to interpret what a series of measurements are indicating relative to the target molecule or process; includes simply derived endpoints such as percent activity, as well as more complex results that aggregate other results, such as IC50, Ki, LD50, or multiparametric activity profiles.
            </em></dd>
            <dt>result -> result detail</dt>
            <dd><em>Additional context for result types defined for an assay, such as parameters used in curve fitting or derived results; also includes the threshold and signal direction used to call actives (hits) for an assay as well as reported quality assessment parameters.</em>
            </dd>
            <dt>project management -> experiment</dt>
            <dd><em>Contextual information about a particular instance ("run") of a defined <strong>assay protocol</strong>, including its role in a larger screening campaign, whether perturbagens were tested at single point or dose response, and the perturbagen collection tested.
            </em></dd>
            <dt>project management -> depositor information</dt>
            <dd><em>Information about the laboratory executing the particular instance (run) of an <strong>assay protocol</strong> definition.
            </em></dd>
            <dt>project management -> project information</dt>
            <dd><em>Broader contextual information about the related experiments and goals for an <strong>experiment</strong> with a defined <strong>assay protocol</strong>; includes the overall goal of the project, such as "inhibitor probe for molecular process X" or "lead for treatment of disease Y".
            </em></dd>
        </dl>

    </div>
</div>

</body>
</html>
