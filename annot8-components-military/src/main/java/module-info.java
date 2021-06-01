module uk.gov.dstl.annot8.military {
  requires io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires io.annot8.components.gazetteers;
  requires io.annot8.components.stopwords;
  requires io.annot8.utils.text;

  exports uk.gov.dstl.annot8.military.processors;
}
