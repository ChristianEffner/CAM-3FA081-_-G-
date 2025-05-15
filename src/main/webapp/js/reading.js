/* ------------------------------------------------------------------
 *  Reading – Schnellsuche & erweitertes Filter-Modal (komplette Datei)
 * ------------------------------------------------------------------ */
document.addEventListener("DOMContentLoaded", () => {

  const apiBaseUrl = "http://localhost:8080";

  /* ================= Hilfs-UUID ==================================== */
  function generateUUID(){
    let d=Date.now(), d2=(performance&&performance.now&&(performance.now()*1000))||0;
    return"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g,c=>{
      let r=Math.random()*16;
      if(d>0){r=(d+r)%16|0; d=Math.floor(d/16);}
      else   {r=(d2+r)%16|0; d2=Math.floor(d2/16);}
      return(c==="x"?r:(r&0x3|0x8)).toString(16);
    });
  }

  /* ================= Globale Variablen ============================ */
  let allReadings=[], allCustomers=[], currentReading=null;

  /* ================= Filter-State ================================= */
  const filter={
    quick :"",
    kind  :"",
    start :"",
    end   :"",
    first :"",
    last  :""
  };

  /* ========================== Daten laden ========================= */
  async function loadReadings(){
    try{
      const userId=localStorage.getItem("userId");
      if(!userId) throw new Error("Kein gültiger Benutzer.");
      const res=await fetch(`${apiBaseUrl}/readings?userId=${userId}`);
      if(!res.ok) throw new Error("Fehler beim Laden (HTTP "+res.status+")");
      allReadings=await res.json();
      applyFilter();
    }catch(e){console.error(e); alert(e.message);}
  }

  /* ===================== Tabelle & Filter ========================= */
  function applyFilter(){
    const list=allReadings.filter(r=>{
      if(filter.kind  && r.kindOfMeter!==filter.kind) return false;
      if(filter.start && r.dateOfReading<filter.start) return false;
      if(filter.end   && r.dateOfReading>filter.end)   return false;
      if(filter.first && !(r.customer?.firstName||"").toLowerCase().includes(filter.first)) return false;
      if(filter.last  && !(r.customer?.lastName ||"").toLowerCase().includes(filter.last )) return false;
      if(filter.quick){
        const hay=(r.id+" "+(r.customer?.firstName||"")+" "+(r.customer?.lastName||"")+" "+r.kindOfMeter).toLowerCase();
        if(!hay.includes(filter.quick)) return false;
      }
      return true;
    });
    renderTable(list);
  }

function formatDate(value) {
  // 1. Array ([yyyy,mm,dd]) abfangen
  if (Array.isArray(value) && value.length === 3) {
    const [year, month, day] = value;
    // führende Nullen
    const mm = String(month).padStart(2, "0");
    const dd = String(day).padStart(2, "0");
    return `${dd}.${mm}.${year}`;
  }

  // 2. echtes Date-Objekt
  if (value instanceof Date) {
    const iso = value.toISOString().slice(0, 10);
    return formatDate(iso); // rekursiv ins Array-Handling? oder direkt splitten
  }

  // 3. String "yyyy-mm-dd"
  if (typeof value === "string") {
    const m = value.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
    if (m) {
      const [, yyyy, mm, dd] = m;
      return `${dd.padStart(2,"0")}.${mm.padStart(2,"0")}.${yyyy}`;
    }
    return value;
  }

  // 4. sonst
  return String(value);

  // jetzt sicher nur Strings matchen
  const m = iso.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (m) {
    const [, yyyy, mm, dd] = m;
    return `${dd}.${mm}.${yyyy}`;
  }
  return iso;
}

  function renderTable(list){
    const tbody=document.getElementById("readingTableBody");
    tbody.innerHTML="";
    list.forEach(r=>{
      tbody.insertAdjacentHTML("beforeend",`
        <tr>
          <td>${r.id}</td>
          <td>${r.customer?.firstName||"?"} ${r.customer?.lastName||""}</td>
          <td>${r.kindOfMeter}</td>
          <td>${formatDate(r.dateOfReading)}</td>
          <td>${r.meterCount}</td>
          <td>
            <button class="btn btn-warning btn-sm btn-edit"   data-id="${r.id}">Bearbeiten</button>
            <button class="btn btn-danger  btn-sm btn-delete" data-id="${r.id}">Löschen</button>
            <button class="btn btn-info   btn-sm btn-data"   data-id="${r.id}">Daten</button>
          </td>
        </tr>`);
    });
    attachRowEvents();
  }

  function attachRowEvents(){
    document.querySelectorAll(".btn-edit").forEach(b=>b.onclick=handleEdit);
    document.querySelectorAll(".btn-delete").forEach(b=>b.onclick=handleDelete);
    document.querySelectorAll(".btn-data").forEach(b=>b.onclick=handleData);
  }

  function handleData(e) {
    const id = e.target.dataset.id;
    const params = new URLSearchParams({
      readingId: id,
      kind:   filter.kind,
      start:  filter.start,
      end:    filter.end,
      first:  filter.first,
      last:   filter.last,
      quick:  filter.quick
    });
    window.location.href = `reading_data.html?${params.toString()}`;
  }

  /* ===================== Bearbeiten / Löschen ===================== */
  async function handleEdit(e){
    const id=e.target.dataset.id;
    try{
      const res=await fetch(`${apiBaseUrl}/readings/${id}`);
      if(!res.ok) throw new Error("Fehler beim Laden (HTTP "+res.status+")");
      currentReading=await res.json();
      document.getElementById("editReadingId").value     =currentReading.id||"";
      document.getElementById("editKindOfMeter").value   =currentReading.kindOfMeter||"";
      document.getElementById("editMeterCount").value    =currentReading.meterCount||0;
      document.getElementById("editDateOfReading").value =currentReading.dateOfReading||"";
      document.getElementById("editComment").value       =currentReading.comment||"";
      new bootstrap.Modal(document.getElementById("editReadingModal")).show();
    }catch(e){console.error(e); alert(e.message);}
  }

  async function handleDelete(e){
    const id=e.target.dataset.id;
    if(!confirm("Soll diese Ablesung wirklich gelöscht werden?")) return;
    try{
      const res=await fetch(`${apiBaseUrl}/readings/${id}`,{method:"DELETE"});
      if(!res.ok) throw new Error("Fehler beim Löschen (HTTP "+res.status+")");
      alert("Ablesung gelöscht.");
      loadReadings();
    }catch(e){console.error(e); alert(e.message);}
  }

  /* ===================== Schnellsuche ============================= */
  document.getElementById("quickSearch").addEventListener("input",e=>{
    filter.quick=e.target.value.toLowerCase();
    applyFilter();
  });

  /* ===================== Advanced-Modal =========================== */
  const advForm=document.getElementById("advFilterForm");
  const advModal=new bootstrap.Modal(document.getElementById("advFilterModal"));

  advForm.addEventListener("submit",e=>{
    e.preventDefault();
    filter.kind =document.getElementById("fKind").value;
    filter.start=document.getElementById("fStart").value;
    filter.end  =document.getElementById("fEnd").value;
    filter.first=document.getElementById("fFirst").value.trim().toLowerCase();
    filter.last =document.getElementById("fLast").value.trim().toLowerCase();
    advModal.hide();
    applyFilter();
  });

  document.getElementById("resetFilter").onclick=()=>{
    advForm.reset();
    filter.kind=filter.start=filter.end=filter.first=filter.last="";
    applyFilter();
  };

  /* =================== Kunden für Modal =========================== */
  async function loadCustomersForReadingModal(){
    const userId=localStorage.getItem("userId"); if(!userId) return;
    try{
      const res=await fetch(`${apiBaseUrl}/customers?userId=${userId}`);
      if(!res.ok) throw new Error("Error loading customers (HTTP "+res.status+")");
      allCustomers=await res.json();
      populateCustomerDropdown(allCustomers);
      document.getElementById("customerSelect").addEventListener("change",toggleNewCustomerFields);
    }catch(e){console.error(e);}
  }

  function populateCustomerDropdown(customers){
    const sel=document.getElementById("customerSelect");
    if(!sel) return;
    sel.innerHTML=`<option value="">-- Neuer Kunde --</option>`;
    customers.forEach(c=>{
      const opt=document.createElement("option");
      opt.value=c.id; opt.textContent=`${c.firstName} ${c.lastName}`; sel.appendChild(opt);
    });
  }

  function toggleNewCustomerFields(){
    const sel=document.getElementById("customerSelect");
    const wrap=document.getElementById("newCustomerFields");
    if(sel.value===""){
      wrap.querySelectorAll("input,select").forEach(el=>el.disabled=false);
      const idInp=document.getElementById("customerId");
      if(!idInp.value) idInp.value=generateUUID();
      wrap.style.display="block";
    }else{
      wrap.querySelectorAll("input,select").forEach(el=>el.disabled=true);
      wrap.style.display="none";
    }
  }

  /* =================== Neues Modal reset / MeterID ================ */
  const addReadingModal=document.getElementById("addReadingModal");
  addReadingModal.addEventListener("show.bs.modal",()=>{
    document.getElementById("comment").value="";
    document.getElementById("dateOfReading").value="";
    document.getElementById("kindOfMeter").selectedIndex=0;
    document.getElementById("meterCount").value="";
    document.getElementById("meterId").value="";
    document.getElementById("substitute").value="0";
    document.getElementById("customerId").value="";
    loadCustomersForReadingModal();
    toggleNewCustomerFields();
  });

  const kindSel=document.getElementById("kindOfMeter");
  kindSel.addEventListener("change",()=>{
    const t=kindSel.value; if(!t)return;
    const cnt=allReadings.filter(r=>r.kindOfMeter===t).length;
    document.getElementById("meterId").value=t+String(cnt+1).padStart(3,"0");
  });

// ————— UPDATE-BUTTON im Edit-Modal —————
document.getElementById("updateReadingBtn").addEventListener("click", async () => {
  const id      = document.getElementById("editReadingId").value;
  const kind    = document.getElementById("editKindOfMeter").value;
  const count   = parseFloat(document.getElementById("editMeterCount").value);
  const date    = document.getElementById("editDateOfReading").value;
  const comment = document.getElementById("editComment").value.trim();

  if (!kind || isNaN(count) || !date) {
    alert("Bitte alle Pflichtfelder im Formular ausfüllen.");
    return;
  }

  // Baue das Payload-Objekt – hier kannst du currentReading.customer etc. nachladen, falls nötig
  const updatedReading = {
    comment,
    customer:   currentReading.customer,
    dateOfReading: date,
    kindOfMeter:   kind,
    meterCount:    count,
    meterId:       currentReading.meterId,
    substitute:    currentReading.substitute
  };

  try {
    const res = await fetch(`${apiBaseUrl}/readings/${id}`, {
      method:  "PUT",
      headers: { "Content-Type": "application/json" },
      body:    JSON.stringify(updatedReading)
    });
    if (!res.ok) throw new Error(`Update fehlgeschlagen (HTTP ${res.status})`);

    alert("Ablesung erfolgreich aktualisiert.");
    // Modal schließen
    bootstrap.Modal.getInstance(document.getElementById("editReadingModal")).hide();
    // Tabelle neu laden
    loadReadings();
  } catch (err) {
    console.error(err);
    alert(err.message);
  }
});

  /* ===================== Neue Ablesung speichern ================== */
  document.getElementById("saveReadingBtn").addEventListener("click",async()=>{
    const comment=document.getElementById("comment").value.trim();
    const date=document.getElementById("dateOfReading").value.trim();
    const kind=document.getElementById("kindOfMeter").value;
    const count=parseFloat(document.getElementById("meterCount").value);
    const meterId=document.getElementById("meterId").value.trim();
    const subst=document.getElementById("substitute").value.trim();
    const substitute=(subst==="1"||subst.toLowerCase()==="true");

    const customerSelect=document.getElementById("customerSelect");
    let customer;
    if(customerSelect&&customerSelect.value){
      customer=allCustomers.find(c=>c.id===customerSelect.value);
    }else{
      let customerId=document.getElementById("customerId").value.trim();
      if(!customerId) customerId=generateUUID();
      const first=document.getElementById("customerFirstName").value.trim();
      const last =document.getElementById("customerLastName").value.trim();
      const birth=document.getElementById("customerBirthDate").value.trim();
      const gender=document.getElementById("customerGender").value;
      if(!first||!last||!birth){alert("Bitte alle Kundenfelder ausfüllen.");return;}
      customer={id:customerId,firstName:first,lastName:last,birthDate:birth,gender,userId:localStorage.getItem("userId")};
    }

    if(!kind||isNaN(count)||!date){alert("Bitte alle Pflichtfelder ausfüllen.");return;}

    const newReading={comment,customer,dateOfReading:date,kindOfMeter:kind,meterCount:count,
                      meterId:meterId||kind+"001",substitute};
    try{
      const res=await fetch(`${apiBaseUrl}/readings`,{
        method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(newReading)});
      if(!res.ok) throw new Error("Fehler beim Speichern (HTTP "+res.status+")");
      alert("Ablesung gespeichert.");
      bootstrap.Modal.getInstance(addReadingModal).hide();
      loadReadings();
    }catch(e){console.error(e); alert(e.message);}
  });

  /* ======================= GO ====================================== */
  loadReadings();
});
