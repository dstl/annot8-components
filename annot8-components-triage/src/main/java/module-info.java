module uk.gov.dstl.annot8.triage {
  requires io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires io.annot8.components.stopwords;
  requires java.json.bind;

  exports uk.gov.dstl.annot8.triage.processors;
}
