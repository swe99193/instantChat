import { useEffect } from "react";
import { Navigate } from "react-router-dom";

// redux
import { useAppSelector } from './redux/hooks';

/**
 * wrapper for protected routes
 */
function ProtectedRoute({ children }) {
    const status = useAppSelector(state => state.login.status); // Redux

    // if logged in, render children component
    if (status == "init")
        return (<></>);     // blank page during authentication (better user experience)
    else if (status == "logout")
        return (<Navigate to="/login" />);
    else    // status == "login"
        return children;

}

export default ProtectedRoute;