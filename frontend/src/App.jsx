import React from "react";

import { useState } from "react";
import StepEmail from "./components/StepEmail";
import StepRUC from "./components/StepRUC";
import StepVehiculo from "./components/StepVehiculo";
import StepANT from "./components/StepANT";
import ResultPanel from "./components/ResultPanel";
import "./styles.css";

const STEPS = [
  { id: 1, label: "Identificación", icon: "✉" },
  { id: 2, label: "RUC / Contribuyente", icon: "📋" },
  { id: 3, label: "Vehículo", icon: "🚗" },
  { id: 4, label: "Licencia ANT", icon: "🪪" },
];

export default function App() {
  const [step, setStep] = useState(1);
  const [data, setData] = useState({
    email: "",
    ruc: "",
    contribuyente: null,
    persona: null,
    placa: "",
    vehiculo: null,
    cedula: "",
    ant: null,
  });

  const next = (patch) => {
    setData((d) => ({ ...d, ...patch }));
    setStep((s) => s + 1);
  };
  const reset = () => { setStep(1); setData({ email:"", ruc:"", contribuyente:null, persona:null, placa:"", vehiculo:null, cedula:"", ant:null }); };

  return (
    <div className="app-root">
      <header className="app-header">
        <span className="logo-mark">SRI<span className="logo-dot">·</span>ANT</span>
        <h1 className="app-title">Verificación Integrada</h1>
        <p className="app-sub">Sistema de consulta SRI y Agencia Nacional de Tránsito</p>
      </header>

      {step <= 4 && (
        <nav className="step-nav" aria-label="Pasos del proceso">
          {STEPS.map((s) => (
            <div key={s.id} className={`step-dot ${step === s.id ? "active" : ""} ${step > s.id ? "done" : ""}`}>
              <span className="dot-icon">{step > s.id ? "✓" : s.icon}</span>
              <span className="dot-label">{s.label}</span>
            </div>
          ))}
        </nav>
      )}

      <main className="wizard-main">
        {step === 1 && <StepEmail onNext={next} initial={data.email} />}
        {step === 2 && <StepRUC onNext={next} email={data.email} initial={data.ruc} />}
        {step === 3 && <StepVehiculo onNext={next} persona={data.persona} initial={data.placa} />}
        {step === 4 && <StepANT onNext={next} cedula={data.ruc?.substring(0,10)} placa={data.placa} />}
        {step === 5 && <ResultPanel data={data} onReset={reset} />}
      </main>

      <footer className="app-footer">
        <span>Datos obtenidos en tiempo real desde SRI y ANT · Ecuador</span>
      </footer>
    </div>
  );
}
