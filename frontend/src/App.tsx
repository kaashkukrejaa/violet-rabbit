import CourseBrowser from './components/CourseBrowser';
import Dashboard from './components/Dashboard';
import Header from './components/Header';
import ScheduleBuilder from './components/ScheduleBuilder';
import Toast from './components/Toast';

export default function App() {
  return (
    <div className="app-shell">
      <Header />
      <main className="app-main">
        <section className="left-column">
          <Dashboard />
          <ScheduleBuilder />
        </section>
        <section className="right-column">
          <CourseBrowser />
        </section>
      </main>
      <Toast />
    </div>
  );
}
