import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';


export interface LoginState {
    loggedin: boolean;
    userId: string;
}

const initialState: LoginState = {
    loggedin: false,
    userId: ""
};

export const loginSlice = createSlice({
    name: 'login',
    initialState,
    // The `reducers` field lets us define reducers and generate associated actions
    reducers: {
        login: (state, action) => {
            state.loggedin = true;
            state.userId = action.payload;
        },
        logout: (state) => {
            state.loggedin = false;
            state.userId = "";
        },
    },
});

export const { login, logout } = loginSlice.actions;

export default loginSlice.reducer;
