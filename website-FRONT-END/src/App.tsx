import { BrowserRouter, Route, Routes } from "react-router";
import SignInPage from "./pages/SignInPage";
import SignUpPage from "./pages/SignUpPage";
import ChatAppPage from "./pages/ChatAppPage";
import { Toaster } from "sonner";

function App() {

  return (<> 
    <Toaster richColors />
    <BrowserRouter>
      <Routes>

        {/* Public Routes */}
        <Route
          path="/signin"
          element={<SignInPage />}
        />
        <Route
          path="/signup"
          element={<SignUpPage />}
        />

        {/* Private Routes */}
        <Route
          path="/"
          element={<ChatAppPage />}
        />
      </Routes>
    </BrowserRouter>
    </>
  );
}

export default App
