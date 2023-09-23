import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';


export interface LoginState {
    status: "init" | "login" | "logout";
    userId: string;
}

const initialState: LoginState = {
    status: "init",
    userId: ""
};

export const loginSlice = createSlice({
    name: 'login',
    initialState,
    // The `reducers` field lets us define reducers and generate associated actions
    reducers: {
        login: (state, action) => {
            state.status = "login";
            state.userId = action.payload;
        },
        logout: (state) => {
            state.status = "logout";
            state.userId = "";
        },
    },
});

export const { login, logout } = loginSlice.actions;

export default loginSlice.reducer;
