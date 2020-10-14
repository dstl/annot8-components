package uk.gov.dstl.annot8.military.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("British Ranks")
@ComponentDescription("Extracts British military rank abbreviations from text")
@ComponentTags({"military", "ranks"})
public class BritishRanks extends AbstractProcessorDescriptor<BritishRanks.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings noSettings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PERSON, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    public Processor() {
      super(
          Pattern.compile(
              "\\b(?<rank>(Flt Lt|Lt Gen|Lt Col|Air Cdre|Gp Capt|Wg Cdr|Sqn Sgt Maj|"
                  + "Adm|VAdm|RAdm|Cdre|Lt Cdr|Cdr|Sub Lt|Lt|SLt|Mid|OC|WO1|WO2|CPO|PO|AB|"
                  + "FM|Gen|Maj Gen|Brig|Col|Maj|Capt|2Lt|OCdt|SSgt|CSgt|Sgt|Cpl|LCpl|Pte|"
                  + "MRAF|Air Chf Mshl|Air Mshl|AVM|Sqn Ldr|Fg Off|Plt Off|Off Cdt|WO|FS|Chf Tech|SAC Tech|SAC ?\\(T\\)|SAC|LAC|MAcr"
                  + "Kgn|Rfn|LSgt|Cfn|Gdmn)|"
                  + "((Flight |Sub |Second )?Lieutenant|Lieutenant( General| Colonel)?|(Air )?Commodore|(Group )?Captain|Wing Commander|(Squadron Sergeant )?Major|"
                  + "(Vice |Rear )?Admiral|(Lieutenant )?Commander|Midshipman|Officer Cadet|Warrant Officer( [12])?|(Chief )?Petty Officer|(Leading|Able) Rating)|"
                  + "RAF Master Aircrew|RAF Flight Sergeant Aircrew|RAF Sergeant Aircrew"
                  + "Field Marshal|(Major )?General|Brigadier|Colonel|(Staff | Colour |Color |Flight |Lance )?Sergeant|(Lance )?Corporal|Private|"
                  + "Marshal of the Royal Air Force|Air (Chief |Vice-)?Marshal|Squadron Leader|(Flying|Pilot) Officer|Chief Technician|Senior Aircraftman \\(?Tech(nician)?\\)?|(Senior |Leading )?Aircraftman|"
                  + "Kingsman|Rifleman|Craftsman|Guardsman)"
                  + "(?<name>( [A-Z][-'A-Za-z]*)+)\\b"),
          0,
          AnnotationTypes.ANNOTATION_TYPE_PERSON);
    }

    @Override
    protected void addProperties(Annotation.Builder builder, Matcher m) {
      builder
          .withProperty("rank", m.group("rank"))
          .withProperty(PropertyKeys.PROPERTY_KEY_NAME, m.group("name").trim());
    }
  }
}
