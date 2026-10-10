import org.luach364.app.Luach;
import java.time.*;
public class LuachTest {
  static int ok=0, bad=0;
  static void chk(String what, Object got, Object exp){ if(String.valueOf(got).equals(String.valueOf(exp))) ok++; else { bad++; System.out.println("FAIL "+what+" got="+got+" exp="+exp);} }
  public static void main(String[] a){
    Luach.Z z;
    z=Luach.zadok(LocalDate.of(2019,3,20)); chk("anchor",z.year+"/"+z.month+"/"+z.day+" "+z.wd+" "+z.cin+" "+z.isX,"1/1/1 3 גמול true");
    z=Luach.zadok(LocalDate.of(2019,5,5)); chk("5.5.19",z.month+"/"+z.day+" "+z.wd+" "+z.cin+" "+z.isD,"2/17 0 מלכיה true");
    z=Luach.zadok(LocalDate.of(2019,7,21)); chk("wine",z.month+"/"+z.day+" "+z.cin+" "+z.feast,"5/3 אמר מועד היין · ביכורי תירוש");
    z=Luach.zadok(LocalDate.of(2019,9,13)); chk("13.9",z.month+"/"+z.day+" "+z.wd+" "+z.cin+" "+z.isX,"6/27 5 דליהו true");
    z=Luach.zadok(LocalDate.of(2022,3,16)); chk("y4",z.year+"/"+z.month+"/"+z.day+" "+z.cin+" "+z.isX,"4/1/1 שכניהו true");
    z=Luach.zadok(LocalDate.of(2025,3,14)); chk("ic",z.ic+" "+z.icDay+" "+z.cin,"true 3 גמול");
    chk("8.3.25",Luach.zadok(LocalDate.of(2025,3,8)).cin,"גמול");
    chk("21.3.25",Luach.zadok(LocalDate.of(2025,3,21)).cin,"גמול");
    chk("22.3.25",Luach.zadok(LocalDate.of(2025,3,22)).cin,"דליהו");
    z=Luach.zadok(LocalDate.of(2025,4,1)); chk("pesach",z.year+" "+z.month+"/"+z.day+" "+z.wd+" "+z.cin+" "+z.feast,"1 1/14 2 מעזיהו פסח");
    z=Luach.zadok(LocalDate.of(2025,4,13)); chk("omer",z.month+"/"+z.day+" "+z.wd+" "+z.cin+" "+z.countN,"1/26 0 ידעיה 1");
    z=Luach.zadok(LocalDate.of(2026,9,16)); chk("teruah",z.year+" "+z.month+"/"+z.day,"2 7/1");
    chk("shm1",Luach.zadok(LocalDate.of(2025,10,9)).shmita,true);
    chk("shm2",Luach.zadok(LocalDate.of(2026,10,7)).shmita,true);
    chk("shm3",Luach.zadok(LocalDate.of(2026,10,8)).shmita,false);
    z=Luach.zadok(LocalDate.of(2019,4,20)); chk("4Q325",z.month+"/"+z.day+" "+z.wd+" "+z.cin+" "+z.cout,"2/2 6 חרים ידעיה");
    z=Luach.zadok(LocalDate.of(2026,10,9)); chk("today",Luach.gem(z.day)+" "+z.month+" "+z.cin+" "+z.toX,"כ״ד 7 הקוץ 14");
    chk("gem",Luach.gem(15)+Luach.gem(30)+Luach.gem(1),"ט״ול׳א׳");
    ZoneId tz=ZoneId.of("Asia/Jerusalem");
    Luach.State s=Luach.stateAt(ZonedDateTime.of(2026,10,9,14,46,0,0,tz),31.668,34.574);
    chk("sun",Luach.hhmm(s.rise)+" "+Luach.hhmm(s.set),"6:40 18:16");
    chk("part",s.isDay+" "+s.part+"/"+s.of+" "+s.gate+" next="+s.nextChange.toLocalTime().withSecond(0).withNano(0),"true 6/8 3 next=15:22");
    s=Luach.stateAt(ZonedDateTime.of(2026,10,10,5,20,0,0,tz),31.668,34.574);
    chk("night",s.beforeSunrise+" "+Luach.gem(s.z.day)+" "+s.isDay+" "+s.part+"/"+s.of,"true כ״ד false 9/10");
    s=Luach.stateAt(ZonedDateTime.of(2026,10,9,22,20,0,0,tz),31.668,34.574);
    chk("night2",s.part+"/"+s.of+" next="+s.nextChange.toLocalTime().withSecond(0).withNano(0),"4/10 next=23:14");
    s=Luach.stateAt(ZonedDateTime.of(2026,10,9,23,50,0,0,tz),31.668,34.574);
    chk("midnight",s.nextChange.toLocalTime().toString(),"00:00");
    System.out.println(ok+" ok, "+bad+" fail");
  }
}
