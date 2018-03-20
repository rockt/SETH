# FIMDA: Finding Mutations in the Digital Age

This file intends to document the integration of the software component “SNP Extraction Tool for Human Variations“ (SETH) into the OpenMinTeD platform.

The focus rests on software development and integration.

## Milestones
The following top level milestones do not strictly depend on each other:
1. UIMA XMI data format serialization for SETH output
2. SETH REST endpoint
3. dockerized SETH

## Implementation

### UIMA XMI serialization
TODO:
- [ ] acquire general knowledge about UIMA XMI:
    * Analysis Engines (AEs) produce Analysis Results (ARs): [intro](https://uima.apache.org/d/uimaj-3.0.0/tutorials_and_users_guides.html#ugr.tug.aae)
    * *Annotators* (e.g. SETH) produce *Annotations*
    * an AR is represented as CAS (Common Analysis Structure): [intro](https://uima.apache.org/d/uimaj-3.0.0/overview_and_setup.html#ugr.ovv.conceptual.representing_results_in_cas), [references](https://uima.apache.org/d/uimaj-3.0.0/references.html#ugr.ref.cas)
    * a CAS contains the analyzed document, a type system and annotations
- [ ] identify relevant UIMA XMI concepts/components e.g. *CAS types*:
    * annotation (describes a region of a document) -> MutationMention 
    * (entity -> Mutation)
- [ ] implement relevant CAS types
    * use JCas: [reference](https://uima.apache.org/d/uimaj-current/references.html#ugr.ref.jcas)
    * [UIMA annotator tutorial](https://uima.apache.org/doc-uima-annotator.html)
    * [version 3 user guide](https://uima.apache.org/d/uimaj-3.0.0/version_3_users_guide.html)
    * create description xml
    * convert description xml to java class, use [JCasGen](https://uima.apache.org/d/uimaj-current/tools.html#ugr.tools.jcasgen): 
        * requires UIMA SDK [installed](https://uima.apache.org/doc-uima-examples.html) 
        * execute: `PATH/TO/UIMA-SDK/bin/jcasgen.sh PATH/TO/INPUT_DESCRIPTION.xml PATH/TO/OUTPUT/DIRECTORY`
            example: `/opt/apache-uima/bin/jcasgen.sh /home/arne/devel/Java/SETH/desc/MutationAnnotation.xml /home/arne/devel/Java/SETH/src/main/java`
